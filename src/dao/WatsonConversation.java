package dao;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Stateful;
import javax.ejb.Stateless;

import com.ibm.watson.developer_cloud.conversation.v1.Conversation;
import com.ibm.watson.developer_cloud.conversation.v1.model.Context;
import com.ibm.watson.developer_cloud.conversation.v1.model.InputData;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageOptions;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;

import service.EBankingService;

@Stateless
@LocalBean
public class WatsonConversation {
	private String workspaceId = "";
	private Conversation service = null;
	private MessageOptions options = null;
	private static HashMap<String, Context> mem = new HashMap<String, Context>();
	private static final String CLASS_NAME = WatsonConversation.class.getName();
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
	
	public MessageResponse message(String input, String conversationId) {
		InputData data = new InputData.Builder(input).build();
		Context context = mem.get(conversationId);
		if(context == null){
			context = new Context();
			context.put("currency", "PLN");
			context.put("timezone", "Europe/Warsaw");
		}
		options = options.newBuilder().input(data).context(context).build();
		MessageResponse response = service.message(options).execute();
		context = response.getContext();
		
		mem.put(context.get("conversation_id").toString(),context);
		//System.out.println(context.get("conversation_id").toString()+"::"+context);
		return response;
	}

	@PostConstruct
	public void init() {
		Properties properties = new Properties();
		InputStream inputStream = getClass().getResourceAsStream("/META-INF/eBanking.properties");
		try {
			properties.load(inputStream);
		} catch (IOException e) {
			System.out.println("Can't load eBanking.properties file");
		}
		service = new Conversation(Conversation.VERSION_DATE_2017_05_26);
		service.setUsernameAndPassword(properties.getProperty("ConversationUsername"), properties.getProperty("ConversationPassword"));
		workspaceId = properties.getProperty("ConversationWorkspaceId");
		/*
		service.createEntity(new CreateEntityOptions.Builder(workspaceId, "Jan Kowalski").addValue(new CreateValue.Builder("Janek").addSynonym("Kowalski").addSynonym("Jan").build()).build()).execute();
		service.createEntity(new CreateEntityOptions.Builder(workspaceId, "Emilia Sokolowska").addValue(new CreateValue.Builder("Emilia").addSynonym("Sokolowska").addSynonym("Emila").build()).build()).execute();
		service.createEntity(new CreateEntityOptions.Builder(workspaceId, "Maria Nowak").addValue(new CreateValue.Builder("Marysia").addSynonym("Nowak").addSynonym("Maria").build()).build()).execute();
		service.createEntity(new CreateEntityOptions.Builder(workspaceId, "Edward Kozlowski").addValue(new CreateValue.Builder("Edward").addSynonym("Kozlowski").addSynonym("Edek").build()).build()).execute();
		*/
		options = new MessageOptions.Builder(workspaceId).build();
		LOGGER.fine("WatsonConversation ########################## postConstruct");

	}

/*
	public void createEntity(String name) throws IOException, JSONException {
		String stringUrl = "https://gateway.watsonplatform.net/conversation/api/v1/workspaces/a7b08d72-8b26-439a-a37d-aea5fd6a07bc/entities/user/values?version=2017-05-26";
        URL url = new URL(stringUrl);
        URLConnection uc = url.openConnection();

        String basicAuth = "Basic " + Base64.getEncoder().encodeToString(("9e572a28-8276-48c9-bdeb-c7443afe78dc:03YroReZwXCy".getBytes()));
        uc.setRequestProperty("Content-Type", "application/json");
        uc.setRequestProperty("Authorization", basicAuth);
        
        uc.setDoOutput(true);
        
        BufferedWriter httpRequestBodyWriter = new BufferedWriter(
				new OutputStreamWriter(uc.getOutputStream()));
		httpRequestBodyWriter.write("{\"value\":\""+name+"\"}");
		httpRequestBodyWriter.close();

        InputStreamReader inputStreamReader = new InputStreamReader(uc.getInputStream());
        
    	BufferedReader r = new BufferedReader(inputStreamReader);
		StringBuilder total = new StringBuilder();
		String line;
		while ((line = r.readLine()) != null) {
			total.append(line).append('\n');
		}

		System.out.println("Enering entity" + total);
        // read this input
	}
	*/
}