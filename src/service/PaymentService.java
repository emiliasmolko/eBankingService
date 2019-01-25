package service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;

import com.ibm.watson.developer_cloud.assistant.v1.model.Context;
import com.ibm.watson.developer_cloud.assistant.v1.model.MessageResponse;


import dao.PaymentConversation;


@Path("/payment/")
public class PaymentService {

	private static final String CLASS_NAME = PaymentService.class.getName();
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

	@Inject
	private PaymentConversation conversation;
	private Client translateService;
	private final String[] zlote = {"(?i)\\s?z(ł|l)oty(ch)?('.'|' ')?","(?i)\\s?z(ł|l)ote('.'|' ')?", "(?i)\\s?z(ł|l)('.'|' ')"};
	private final String PLN = " PLN ";
	private final String[] grosze = {" cent ", " cents ", "(?i)\\s?groszy('.'|' ')?", "(?i)\\s?(gr('.')?)(?!\\S)('.')?", " crore "};
	private final String GROSZE = " grosze ";
	private static HashMap<String, ArrayList<String>> mem = new HashMap<String, ArrayList<String>>();
	private static ArrayList<String> receivers;
	
	@PostConstruct
	public void postConstruct() {
		translateService = ClientBuilder.newClient();
		receivers = new ArrayList<String>();
		receivers.add("PGNIG");
		receivers.add("Innogy");
		receivers.add("UPC");
		receivers.add("Szkoła");
		receivers.add("Maria Nowak");
		receivers.add("Jan Kowalski");
		receivers.add("Edward Kozlowski");
		receivers.add("Ewa Smolko");
		receivers.add("Emilia Sokolowska");
		LOGGER.info("EBankingService ########################## postConstruct");
	}

	private String translate(String text) {
		String message = replace(" "+text+" ", zlote, PLN);
		boolean ifPLN = false;
		if (message.indexOf(PLN)>=0) {
			ifPLN = true;
		}
		message = replace(message, grosze, GROSZE);
		
		LOGGER.info("przed translate:"+message);
		message = translateService.target("https://translate.googleapis.com/translate_a/single").queryParam("client", "gtx").queryParam("dt", "t")
		        .queryParam("sl", "pl").queryParam("tl", "en").queryParam("ie", "UTF-8").queryParam("oe", "UTF-8")
		        .queryParam("q", message)
		        .request(MediaType.APPLICATION_JSON)
		        .get(String.class);		
		LOGGER.info("po translate1:"+message);
		message = message.substring(4, message.indexOf("\",\"", 4));
		message = message.replaceAll(",", ".");
		if(ifPLN) {
			message = replace(" "+message+" ", grosze, GROSZE);
		}
		message = (" "+message+" ").replaceAll(PLN, GROSZE);
		message = message.replaceFirst(GROSZE,PLN);
		
		LOGGER.info("po translate3:"+message);
		return message;
	}
	private String replace(String source, String[] array, String target) {
		String text = source;
		for(int i=0; i<array.length; i++) {
			text = text.replaceAll(array[i], target);
		}
		return text;
	}
	@POST
	@Path("/message/{cn}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public List<List<String>> runMessage(@PathParam("cn")String conversationId, String text) {
		LOGGER.info("###########################" +conversationId+"###########"+text );		
  		MessageResponse mr = conversation.message(translate(text), conversationId);
		Context context = mr.getContext();
		ArrayList<String> transaction = null;
		LOGGER.info("runMessage"+mr);
		if (context!= null && context.get("operation")!= null) {
			String operation = context.get("operation").toString();
			if ( operation!= null && operation.equals("TRANSFER")) {
				ArrayList<String> transactions = mem.get(conversationId);
				if(transactions == null) {					
					transactions = new ArrayList<String>();
					mem.put(conversationId, transactions);
				}
				transaction = new ArrayList<String>();
				transaction.add("DATA:" +context.get("transfer_date").toString()+ ", ODBIORCA:"+context.get("receiver").toString()+", KWOTA:"+context.get("amount").toString()+", WALUTA:"+context.get("currency").toString());
				transactions.add(transaction.get(0));
				mr = conversation.message("ok", conversationId);
				LOGGER.info("runMessage"+mr);
			}
		}
		List<List<String>> result = new ArrayList<List<String>>();
		ArrayList<String> tmp = new ArrayList<String>();
		tmp.add(mr.getContext().get("conversation_id").toString());
		result.add(0, tmp);
		result.add(1,transaction);//transfer definition
		result.add(2,null);//map
		result.add(3,null);//enabled many buttons
		result.add(4,mr.getOutput().getText());//text

		return result;
	}
	@GET
	@Path("/transactions/{cn}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getTransactions(@PathParam("cn")String conversationId) {
		return mem.get(conversationId);
	}
	@GET
	@Path("/receivers/{cn}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getReceivers(@PathParam("cn")String conversationId) {
		return receivers;
	}

}
