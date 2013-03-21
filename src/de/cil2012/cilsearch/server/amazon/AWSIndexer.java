package de.cil2012.cilsearch.server.amazon;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.collect.Lists;

import de.cil2012.cilsearch.server.Indexer;
import de.cil2012.cilsearch.server.amazon.cloudsearch.AWSCloudSearchClient;
import de.cil2012.cilsearch.server.amazon.cloudsearch.ChangeIndexItem;
import de.cil2012.cilsearch.server.amazon.cloudsearch.ChangeIndexRequest;
import de.cil2012.cilsearch.server.model.MailContent;
import de.cil2012.cilsearch.server.model.MailObject;
import de.cil2012.cilsearch.server.model.exceptions.ChangeIndexException;
import de.cil2012.cilsearch.shared.MailHelper;

public class AWSIndexer implements Indexer {

	private AWSCloudSearchClient documentService;

	public AWSIndexer(String domainName, String domainId, String region) {

		documentService = AWSCloudSearch.getDocumentServiceClientInstance(domainName, domainId, region);
	}

	/**
	 * internal createSDFItem converts a MailObject to its SDF representation
	 * 
	 * @param mail
	 * @return a ChangeIndexItem object, which represents the SDF structure
	 */
	private ChangeIndexItem createSDFItem(MailObject mail) {
		ChangeIndexItem sdf = new ChangeIndexItem();
		// Part 1 - not-content-related settings
		sdf.setType("add");
		String id = MailHelper.escapeMessageId(mail.getMessageId());
		sdf.setId(id);
		sdf.setVersion((int) (new Date().getTime() / 1000));
		sdf.setLang("en");

		// Part 2 - convert MailObject to SDF format
		MailFields fields = new MailFields();
		fields.setId_search("search_" + id);
		fields.setFolder(mail.getFolder());
		fields.setSubject(mail.getSubject());

		// SimpleDateFormat ISO8601DATEFORMAT = new SimpleDateFormat(
		// "yyyy-MM-dd'T'HH:mm:ssZ", Locale.GERMANY);
		// String date = ISO8601DATEFORMAT.format(mail.getDate());

		// Strip off milliseconds for uint field
		fields.setDate(mail.getDate().getTime() / 1000);

		LinkedList<String> from = new LinkedList<String>();
		Address fromAddr = mail.getFrom();

		if (fromAddr instanceof InternetAddress) {
			InternetAddress inetFrom = (InternetAddress) fromAddr;
			String personal = inetFrom.getPersonal();
			if (personal != null)
				if (!personal.isEmpty())
					from.add(personal);
			from.add(inetFrom.getAddress());
		} else {
			from.add(fromAddr.toString());
		}
		fields.setFrom((String[]) from.toArray(new String[0]));

		List<Address> recp;
		LinkedList<String> recpString = new LinkedList<String>();

		// convert TO-recipients
		recp = mail.getRecipientsTO();
		if (recp == null) {
			recp = new LinkedList<Address>();
		}
		recpString = new LinkedList<String>();
		for (Address addr : recp) {
			recpString.add(addr.toString());
		}

		List<List<String>> recpts = Lists.partition(recpString, 100);
		for (int i = 0; i < recpts.size(); i++) {

			if (i == 0) {
				fields.setRecipients_to((String[]) recpts.get(i).toArray(new String[0]));
			}
			if (i == 1) {
				fields.setRecipients_to1((String[]) recpts.get(i).toArray(new String[0]));
			}
			if (i == 2) {
				fields.setRecipients_to2((String[]) recpts.get(i).toArray(new String[0]));
			}

			if (i == 3) {
				Log.info(mail.getMessageId() + " - there are more than 300 TO-recipients, only first 300 are kept, others are cut off");
				break;
			}
		}

		// convert CC-recipients
		recp = mail.getRecipientsCC();
		if (recp == null) {
			recp = new LinkedList<Address>();
		}
		recpString = new LinkedList<String>();
		for (Address addr : recp) {
			recpString.add(addr.toString());
		}
		if (recpString.size() > 100) {
			recpts = Lists.partition(recpString, 100);
			fields.setRecipients_cc((String[]) recpts.get(0).toArray(new String[0]));
			Log.info(mail.getMessageId() + " - there are more than 100 CC-recipients, only first 100 are kept, others are cut off");
		} else {
			fields.setRecipients_cc((String[]) recpString.toArray(new String[0]));
		}

		// convert BCC-recipients
		recp = mail.getRecipientsBCC();
		if (recp == null) {
			recp = new LinkedList<Address>();
		}
		recpString = new LinkedList<String>();
		for (Address addr : recp) {
			recpString.add(addr.toString());
		}
		if (recpString.size() > 100) {
			recpts = Lists.partition(recpString, 100);
			fields.setRecipients_bcc((String[]) recpts.get(0).toArray(new String[0]));
			Log.info(mail.getMessageId() + " - there are more than 100 BCC-recipients, only first 100 are kept, others are cut off");
		} else {
			fields.setRecipients_bcc((String[]) recpString.toArray(new String[0]));
		}

		// convert content
		// content_other_names are the name entries of attachments
		// content is the actual content of message body and attachments
		// content_main_preview is a preview of the message body (main content)

		LinkedList<String> contentOtherNames = new LinkedList<String>();
		LinkedList<String> content = new LinkedList<String>();
		String contentPreview = "";
		// index NAME and CONTENT and save a short PREVIEW of the main content
		// as a result
		for (int i = 0; i < mail.getContents().size(); i++) {
			content.add(mail.getContents().get(i).getContent());

			MailContent mc = mail.getContents().get(i);
			if (mc.isMainContent()) {
				contentPreview = mc.getContent().substring(0, (mc.getContent().length() > 200) ? 200 : mc.getContent().length() - 1);
			} else {
				contentOtherNames.add(mail.getContents().get(i).getName());
			}

		}
		fields.setContent_other_names((String[]) contentOtherNames.toArray(new String[0]));
		// place
		if (fields.getContent_other_names().length > 0 && fields.getContent_other_names()[0] == null) {
			fields.setContent_other_names(new String[0]);
		}

		fields.setContent((String[]) content.toArray(new String[0]));
		fields.setContent_main_preview(contentPreview);

		sdf.setFields(fields);

		return sdf;
	}

	@Override
	public void addToIndex(List<MailObject> mails) throws ChangeIndexException {
		ChangeIndexRequest request = new ChangeIndexRequest();

		int batchThreshold = 4900; // in KB
		int sumBatch = 0; // in bytes

		StringBuilder requestSDF = new StringBuilder(5000000);
		requestSDF.append("[");

		for (int index = 0; index < mails.size(); index++) {
			MailObject mail = mails.get(index);
			if (mail.getMessageId() != null && mail.getContents() != null) {
				// Escape illegal characters
				if (mail.getSubject() != null) {
					mail.setSubject(mail.getSubject().replaceAll("[^\\u0009\\u000a\\u000d\\u0020-\\uD7FF\\uE000-\\uFFFD]", ""));
				}
				if (mail.getContents() != null) {
					int size = mail.getContents().size();
					for (int i = 0; i < size; i++) {
						String content = mail.getContents().get(i).getContent();
						content = content.replaceAll("[^\\u0009\\u000a\\u000d\\u0020-\\uD7FF\\uE000-\\uFFFD]", "");
						mail.getContents().get(i).setContent(content);
					}
				}

				ChangeIndexItem item = this.createSDFItem(mail);
				request.addToBatch(item);

				// Set the mail object to null to make it disposable
				mail = null;
				mails.set(index, null);

				int documentThreshold = 900; // in KB
				int contentSize = 0; // in bytes

				// Amazons Document size must not exceed 1 MB. this size can
				// usually
				// only be reached by content
				// here we cut the contents so that the document size will not
				// exceed this threshold
				MailFields fields = (MailFields) item.getFields();

				for (String content : fields.getContent()) {
					contentSize += content.length();
				}
				Log.debug("Content size: " + contentSize / 1000);

				// cut contents if above the threshold
				if (contentSize / 1000 > documentThreshold) {
					int leftToCut = contentSize / 1000 - documentThreshold;
					String[] contents = fields.getContent();
					int sum = 0;
					for (int i = 0; i < contents.length; i++) {
						sum += contents[i].length();
					}
					int i = contents.length - 1;

					// cut contents
					// cut big contents more than small contents
					while (leftToCut > 0) {
						int contentLength = contents[i].length();
						int cut;
						if (((double) contentLength / (double) sum) > 0.5d) {
							cut = (int) (0.5d * (double) contentLength);
						} else if (((double) contentLength / (double) sum) > 0.3d) {
							cut = (int) (0.3d * (double) contentLength);
						} else if (((double) contentLength / (double) sum) > 0.1d) {
							cut = (int) (0.1d * (double) contentLength);
						} else {
							cut = (int) (0.05d * (double) contentLength);
						}

						cut += 1; // cut at least one character (to prevent
									// getting
									// stuck in the loop somehow)
						contents[i] = contents[i].substring(0, contentLength - 1 - cut);
						leftToCut -= cut;
						for (int j = 0; j < contents.length; j++) {
							sum += contents[j].length();
						}

						i--;
						if (i < 0)
							i = contents.length - 1;

					}

					contentSize = 0;
					for (String content : fields.getContent()) {
						contentSize += content.length();
					}
					Log.debug("New content size: " + contentSize / 1000);
				}

				// Amazons batch request size MUST not exceed 5 MB
				// we will split the request if we reach this threshold

				String itemString = item.toSDF();
				sumBatch += itemString.length();
				Log.debug("Batch size: " + sumBatch / 1000);

				if (sumBatch / 1000 > batchThreshold) {
					requestSDF.append("\n]");
					Log.debug("Batch size exceeds threshold. Uploading batch");
					documentService.changeIndex(request);

					// Reset request
					request = new ChangeIndexRequest();

					// Reset size and buffer
					sumBatch = itemString.length();
					requestSDF.delete(0, requestSDF.length());
					requestSDF.append("[");
				}
				requestSDF.append(",\n");
				requestSDF.append(itemString);
			}
		}

		requestSDF.append("\n]");

		documentService.changeIndex(request);

	}

	@Override
	public void removeFromIndex(Set<String> messageIds) throws ChangeIndexException {
		ChangeIndexRequest request = new ChangeIndexRequest();

		for (String id : messageIds) {
			id = MailHelper.escapeMessageId(id);

			ChangeIndexItem item = new ChangeIndexItem();
			item.setType("delete");
			item.setId(id);
			item.setVersion((int) (new Date().getTime() / 1000));
			request.addToBatch(item);
		}

		documentService.changeIndex(request);

	}

}
