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

import com.ibm.watson.developer_cloud.conversation.v1.model.Context;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;

import dao.BankAccountDAO;
import dao.WatsonConversation;

@Path("/")
public class EBankingService {

	private static final String CLASS_NAME = EBankingService.class.getName();
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

	@Inject
	private BankAccountDAO dao;
	@Inject
	private WatsonConversation conversation;
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
		LOGGER.fine("EBankingService ########################## postConstruct");
	}

	private String translate(String text) {
		String message = replace(" "+text+" ", zlote, PLN);
		boolean ifPLN = false;
		if (message.indexOf(PLN)>=0) {
			ifPLN = true;
		}
		message = replace(message, grosze, GROSZE);
		
		System.out.println("przed translate:"+message);
		message = translateService.target("https://translate.googleapis.com/translate_a/single").queryParam("client", "gtx").queryParam("dt", "t")
		        .queryParam("sl", "pl").queryParam("tl", "en").queryParam("ie", "UTF-8").queryParam("oe", "UTF-8")
		        .queryParam("q", message)
		        .request(MediaType.APPLICATION_JSON)
		        .get(String.class);		
		System.out.println("po translate1:"+message);
		message = message.substring(4, message.indexOf("\",\"", 4));
		
		if(ifPLN) {
			message = replace(" "+message+" ", grosze, GROSZE);
		}
		message = (" "+message+" ").replaceAll(PLN, GROSZE);
		message = message.replaceFirst(GROSZE,PLN);
		
		System.out.println("po translate3:"+message);
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
	public List<String> runMessage(@PathParam("cn")String conversationId, String text) {
		System.out.println("###########################" +conversationId+"###########"+text );		
  		MessageResponse mr = conversation.message(translate(text), conversationId);
		Context context = mr.getContext();
		System.out.println("runMessage"+mr);
		if (context!= null && context.get("operation")!= null) {
			String operation = context.get("operation").toString();
			if ( operation!= null && operation.equals("TRANSFER")) {
				ArrayList<String> transactions = mem.get(conversationId);
				if(transactions == null) {					
					transactions = new ArrayList<String>();
					mem.put(conversationId, transactions);
				}
				String i = "DATA:" +context.get("transfer_date").toString()+ ", ODBIORCA:"+context.get("receiver").toString()+", KWOTA:"+context.get("amount").toString()+", WALUTA:"+context.get("currency").toString();
				transactions.add(i);
				mr = conversation.message("ok", conversationId);
				System.out.println("runMessage"+mr);
			}
		}
		
		List<String> result = mr.getOutput().getText();
		result.add(0, mr.getContext().get("conversation_id").toString());
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
/*
	public BankAccount logIn(String email_and_password) {
		BankAccount result = null;
		try {
			String[] splited_login_data = email_and_password.split(" ");
			try {
				result = dao.getBankAccountLogIn(splited_login_data[0], splited_login_data[1]);
			} catch (NoResultException e) {
				result = null;
			}

		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		return result;
	}

	public BankAccount logInFR(String email) {
		BankAccount result = null;
		try {
			result = dao.getBankAccountLogIn(email);
		} catch (NoResultException e) {
			result = null;
		}

		return result;
	}
	*/
}
