package dao;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;
import com.ibm.watson.developer_cloud.assistant.v1.Assistant;
import com.ibm.watson.developer_cloud.assistant.v1.model.Context;
import com.ibm.watson.developer_cloud.assistant.v1.model.InputData;
import com.ibm.watson.developer_cloud.assistant.v1.model.MessageOptions;
import com.ibm.watson.developer_cloud.assistant.v1.model.MessageResponse;

@Stateless
@LocalBean
public class PaymentConversation {
	
	private Assistant service = null;
	private MessageOptions options = null;
	private static HashMap<String, Context> mem = new HashMap<String, Context>();
	private static final Logger LOGGER = Logger.getLogger(PaymentConversation.class.getName());
	
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
		String url = null;
		String username = null;
		String password = null;
		String workspaceId = null;
		
		try {
			InputStream inputStream = getClass().getResourceAsStream("/META-INF/eBanking.properties");
			Properties properties = new Properties();
			properties.load(inputStream);
			url = properties.getProperty("payment_URL");
			username = properties.getProperty("payment_ConversationUsername");
			password = properties.getProperty("payment_ConversationPassword");
			workspaceId = properties.getProperty("payment_ConversationWorkspaceId");
		} catch (IOException e2) {
			LOGGER.severe("Can't load eBanking.properties file");
		}
		//try {
			String VCAP_SERVICES = System.getenv("VCAP_SERVICES");
			/*JSONObject vcap = (JSONObject) JSONObject.parse(VCAP_SERVICES);
			LOGGER.info(vcap.toString());
			JSONArray services = (JSONArray) vcap.get("watson Assistant");
			JSONObject instance = (JSONObject) services.get(0);
			JSONObject instanceCredentials = (JSONObject) instance.get("credentials");
			url = (String) instanceCredentials.get("url");
			username = (String) instanceCredentials.get("username");
			password = (String) instanceCredentials.get("pasword");*/
		//} catch (IOException e) {// try local
			
		//	LOGGER.severe("Can't load VCAP_SERVICES");
		//}
		service = new Assistant("2018-09-20");
		service.setUsernameAndPassword(username, password);
		service.setEndPoint(url);
		
		options = new MessageOptions.Builder(workspaceId).build();

	}


}