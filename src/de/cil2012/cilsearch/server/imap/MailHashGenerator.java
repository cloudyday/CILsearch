package de.cil2012.cilsearch.server.imap;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.commons.codec.digest.DigestUtils;

public final class MailHashGenerator {

	private MailHashGenerator() {

	}

	public static String generateHash(Message message) {
		String result = "";
		if (!message.getFolder().isOpen()) {
			try {
				message.getFolder().open(Folder.READ_ONLY);
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
		try {
			result = DigestUtils.md5Hex(message.getHeader("Message-ID")[0] + message.getFrom()[0] + message.getSubject() + message.getReceivedDate());
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		return result;
	}
}
