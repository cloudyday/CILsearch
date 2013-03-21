package de.cil2012.cilsearch.server.elasticsearch;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import com.allen_sauer.gwt.log.client.Log;

import de.cil2012.cilsearch.server.Indexer;
import de.cil2012.cilsearch.server.model.MailContent;
import de.cil2012.cilsearch.server.model.MailObject;
import de.cil2012.cilsearch.server.model.exceptions.ChangeIndexException;
import de.cil2012.cilsearch.shared.MailHelper;

/**
 * This class implements the indexing functionality, i.e. adding and removing
 * documents to/from the index
 * 
 * @see Indexer
 */
public class ESIndexer implements Indexer {

	private String domainName;
	private TransportClient client;

	public ESIndexer(String domainName) {
		this.client = ESSearch.createClient();
		this.domainName = domainName;
	}

	/**
	 * internal transforms a single MailObject to an indexing-query
	 * 
	 * @param mail
	 * @return IndexRequest
	 * @throws IOException
	 */
	private IndexRequest prepareItem(MailObject mail) throws IOException {
		IndexRequest req = new IndexRequest(domainName, "mail", MailHelper.escapeMessageId(mail.getMessageId()));

		XContentBuilder cBuilder = XContentFactory.jsonBuilder();
		cBuilder.startObject();
		cBuilder.field("folder", mail.getFolder());
		cBuilder.field("subject", mail.getSubject());
		cBuilder.field("date", mail.getDate().getTime());

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

		cBuilder.array("from", (String[]) from.toArray(new String[0]));

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

		cBuilder.array("recipients_to", (String[]) recpString.toArray(new String[0]));

		// convert CC-recipients
		recp = mail.getRecipientsCC();
		if (recp == null) {
			recp = new LinkedList<Address>();
		}
		recpString = new LinkedList<String>();
		for (Address addr : recp) {
			recpString.add(addr.toString());
		}
		cBuilder.array("recipients_cc", (String[]) recpString.toArray(new String[0]));

		// convert BCC-recipients
		recp = mail.getRecipientsBCC();
		if (recp == null) {
			recp = new LinkedList<Address>();
		}
		recpString = new LinkedList<String>();
		for (Address addr : recp) {
			recpString.add(addr.toString());
		}
		cBuilder.array("recipients_bcc", (String[]) recpString.toArray(new String[0]));

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

		cBuilder.array("content_other_names", (String[]) contentOtherNames.toArray(new String[0]));

		cBuilder.array("content", (String[]) content.toArray(new String[0]));
		cBuilder.field("content_main_preview", contentPreview);

		cBuilder.endObject();
		req.source(cBuilder);

		return req;
	}

	@Override
	public void addToIndex(List<MailObject> mails) throws ChangeIndexException {
		BulkRequestBuilder bulk = client.prepareBulk();
		BulkResponse response = null;

		// threshold in KB
		int threshold = 4900;
		StringBuilder req = new StringBuilder((int) (threshold * 1000 * 1.01));

		for (MailObject mail : mails) {
			if (mail.getMessageId() != null && mail.getContents() != null) {
				try {
					IndexRequest request = this.prepareItem(mail);
					req.append(request.toString());
					bulk.add(request);

					if (req.length() / 1000 > threshold) {

						try {
							response = bulk.execute().actionGet();

							if (response.hasFailures()) {
								Log.info("BulkRequest Response reported failure");
								throw new ESChangeIndexException(response.buildFailureMessage());
							}

						} catch (Throwable t) {
							Log.info("BulkRequest has failed");
							throw new ESChangeIndexException(t);
						}

						bulk = client.prepareBulk();
						req.delete(0, req.length());

					}

				} catch (IOException e) {
					Log.warn("building an index request has failed");
				}
			}
		}

		try {
			response = bulk.execute().actionGet();

			if (response.hasFailures()) {
				Log.info("BulkRequest Response reported failure");
				throw new ESChangeIndexException(response.buildFailureMessage());
			}

		} catch (Throwable t) {
			Log.info("BulkRequest has failed");
			throw new ESChangeIndexException(t);
		}

	}

	@Override
	public void removeFromIndex(Set<String> messageIds) throws ChangeIndexException {
		BulkRequestBuilder bulk = client.prepareBulk();

		for (String id : messageIds) {
			id = MailHelper.escapeMessageId(id);

			DeleteRequest req = new DeleteRequest(domainName, "mail", id);
			bulk.add(req);
		}

		BulkResponse response;

		try {
			response = bulk.execute().actionGet();
		} catch (Throwable t) {
			Log.info("Request has failed");
			throw new ESChangeIndexException(t.getMessage());
		}

		if (response.hasFailures()) {
			Log.info("Response reported failure");
			throw new ESChangeIndexException(response.buildFailureMessage());
		}

	}

}
