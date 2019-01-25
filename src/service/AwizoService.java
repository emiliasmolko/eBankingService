package service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;

import com.ibm.watson.developer_cloud.assistant.v1.model.Context;
import com.ibm.watson.developer_cloud.assistant.v1.model.MessageResponse;

import dao.AwizoConversation;
import dao.BankDAO;


@Path("/awizo/")
public class AwizoService {

	private static final String CLASS_NAME = AwizoService.class.getName();
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

	//@Inject
	//private Translate translate;
	@Inject
	private AwizoConversation conversation;
	@Inject
	private BankDAO db;
	private Client translateService;
	private final String[] zlote = {"(?i)\\s?z(ł|l)oty(ch)?('.'|' ')?","(?i)\\s?z(ł|l)ote('.'|' ')?", "(?i)\\s?z(ł|l)('.'|' ')"};
	private final String PLN = " PLN ";
	private final String[] grosze = {" cent ", " cents ", "(?i)\\s?groszy('.'|' ')?", "(?i)\\s?(gr('.')?)(?!\\S)('.')?", " crore "};
	private final String GROSZE = " grosze ";

	@PostConstruct
	public void postConstruct() {
		translateService = ClientBuilder.newClient();
	}

	@POST
	@Path("/branch/{cn}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)

	public List<List<String>> branch(@PathParam("cn") String conversationId, String text) {
		Context context = conversation.getContext(conversationId);
		context.put("branchid", text);
		context.put("branch", db.getBranchInfo(text));
		context.remove("action");
		MessageResponse mr = conversation.message(" ", conversationId);
		return runMessage(conversationId, mr);
	}

	@POST
	@Path("/message/{cn}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public List<List<String>> runMessage(@PathParam("cn") String conversationId, String text) {
		LOGGER.info("###########################" + conversationId + "###########" + text);
		MessageResponse mr = conversation.message(this.translate(text), conversationId);
		return runMessage(conversationId, mr);
	}

	private List<List<String>> runMessage(String conversationId, MessageResponse mr) {
		Context context = mr.getContext();
		List<String> disabledButton = null;
		List<String> map = null;
		List<String> buttons = null;
		List<List<String>> result = new ArrayList<List<String>>();
		List<String> tmp = new ArrayList<String>();
		tmp.add(context.get("conversation_id").toString());
		result.add(0, tmp);
		tmp = mr.getOutput().getText();
		String holiday = null;
		LOGGER.info("runMessage" + mr);
		if (context.get("setdate") != null) {
			holiday = db.isHoliday(context.get("awizo_date").toString());
			context.replace("setdate", null);
			conversation.updateContext(context);
			if (holiday != null) {
				tmp = new ArrayList<String>();
				tmp.add("Podana data " + context.get("awizo_date").toString() + " to " + holiday
						+ ". W jakim innym dniu chciałbyś odebrać pieniądze?");
				context.replace("awizo_date", null);
				conversation.updateContext(context);
			}		
		}
		if (holiday== null && context.get("action") != null) {
			String operation = context.get("action").toString();

			switch (operation) {
			case "CITY":
				buttons = db.findCities();
				break;
			case "MAP":
				map = db.findBranches(context.get("location").toString(), context.get("currency_value").toString(),
						context.get("currency_unit").toString(), context.get("awizo_date").toString());
				if (map.size() < 2) {
					context.put("action", "NOBRANCHES");
					conversation.updateContext(context);
					mr = conversation.message(" ", conversationId);
					tmp = mr.getOutput().getText();
					LOGGER.info("runMessage" + mr);
				}
				break;
			case "AWIZO":
				disabledButton = new ArrayList<String>();
				disabledButton.add("Data awizacji: " + context.get("awizo_date").toString() + ", Adres oddziału: "
						+ context.get("branch").toString() + ", Suma gotówki: "
						+ context.get("currency_value").toString() + " " + context.get("currency_unit").toString());
				context.replace("action", null);
				context.replace("currency_value", null);
				context.replace("awizo_date", null);
				context.replace("location", null);
				context.replace("branch", null);
				context.replace("branchid", null);

				conversation.updateContext(context);
				LOGGER.info("AWIZO:" + conversation.message("ok", conversationId));
				break;
			}
		}
		result.add(1, disabledButton);// awizo definition
		result.add(2, map);// map
		result.add(3, buttons);// enabled many buttons
		result.add(4, tmp);// text
		LOGGER.info("result" + result);
		return result;
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
		//message = message.replaceAll(",", ".");
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
}
