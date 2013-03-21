package de.cil2012.cilsearch.server;

import java.io.IOException;
import java.io.InputStream;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.cil2012.cilsearch.server.imap.MailHandler;

public class AttachmentDownloadServlet extends HttpServlet {

	private static final long serialVersionUID = 7117189568441617278L;

	private static final int DEFAULT_BUFFER_SIZE = 10240; // 10 KB
	
	/**
	 * Loads an attachment from the IMAP server and send the given
	 * attachment number back as a downlaod stream. Should be called in 
	 * the following form
	 * 
	 * cilsearch/download?folder=[FOLDER]&msg=[MSG-ID]&num=[ATTACHMENT-NUMBER]
	 */
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String folder = request.getParameter("folder");
		String msgId = request.getParameter("msg");
		String numString = request.getParameter("num");
		
		// If any of our parameters is zero return an error
		if (folder == null || msgId == null || numString == null) {
			response.sendError(400, "Missing parameter");
			return;
		}
		
		// Try to parse attachment number
		Integer attachmentNumber = null;
		try {
			attachmentNumber = Integer.parseInt(numString);
		} catch (NumberFormatException ex) {
			response.sendError(400, "Bad attachment Number");
		}
		
		// Get the message from IMAP server
		MailHandler handler = new MailHandler();
		Message message = handler.findMessageByIdAndFolder(folder, msgId);
		if (message == null) {
			response.sendError(400, "Invalid message-id");
			return;
		}
		
		try {
			// Only multipart messages can contain attachments
			if (!(message.getContent() instanceof MimeMultipart)) {
				response.sendError(400, "Message content is not multipart");
				return;
			}
			MimeMultipart contents = (MimeMultipart)message.getContent();
			
			// Check if the given attachment number exists
			if (contents.getCount() <= attachmentNumber) {
				response.sendError(400, "Given attachment number does not exist");
				return;
			}
			BodyPart content = contents.getBodyPart(attachmentNumber);
			
			// Check if filename and -type are set
			String filename = content.getFileName();
			if (filename == null) {
				filename = "dummy mummy.txt";
			}
			String type = content.getContentType();
			if (type == null) {
				type = "application/octet-stream";
			}
			
			// If type contains a semicolon we have to split it at this point
			// and take only the first part, because the second part is the filename
			// Example: application/pdf;\r\nname="Ubung 9 (1).pdf"
			if (type.contains(";")) {
				type = type.split(";")[0];
			}
			
			// Get contents as an input stream
			InputStream contentDataStream = content.getInputStream();
			
			// Count input stream size
			contentDataStream.mark(Integer.MAX_VALUE);
			int size = 0;
			int len = 0;
			do {
				len = contentDataStream.read(new byte[DEFAULT_BUFFER_SIZE], 0, DEFAULT_BUFFER_SIZE);
				size += len;
			} while (len == DEFAULT_BUFFER_SIZE);
			contentDataStream.reset();
			
			// Load the attachment and send it as a stream
			response.reset();
			response.setBufferSize(DEFAULT_BUFFER_SIZE);
			response.setContentType(type);
			response.setHeader("Content-Length", String.valueOf(size));
			response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
			
			ServletOutputStream stream = response.getOutputStream();
			
			for (int index = 0; index < content.getSize(); index += DEFAULT_BUFFER_SIZE) {
				byte[] data = new byte[DEFAULT_BUFFER_SIZE];
				int length = contentDataStream.read(data, 0, DEFAULT_BUFFER_SIZE);
				
				if (length < DEFAULT_BUFFER_SIZE) {
					System.out.println(data[length-1]);
				}
				
				stream.write(data, 0, length);
			}	
			
			stream.close();
		} catch (Exception e) {
			response.sendError(400, e.getMessage());
		} finally {
			handler.closeConnection();
		}
	}
}
