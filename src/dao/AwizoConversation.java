package dao;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import com.ibm.json.java.JSONObject;
import com.ibm.watson.developer_cloud.assistant.v1.Assistant;
import com.ibm.watson.developer_cloud.assistant.v1.model.Context;
import com.ibm.watson.developer_cloud.assistant.v1.model.InputData;
import com.ibm.watson.developer_cloud.assistant.v1.model.MessageOptions;
import com.ibm.watson.developer_cloud.assistant.v1.model.MessageResponse;

@Stateless
@LocalBean
public class AwizoConversation {
	private String workspaceId = "";
	private Assistant service = null;
	private MessageOptions options = null;
	private static HashMap<String, Context> mem = new HashMap<String, Context>();
	private static final Logger LOGGER = Logger.getLogger(AwizoConversation.class.getName());
	
	public MessageResponse message(String input, String conversationId) {
		InputData data = new InputData.Builder(input).build();
		Context context = mem.get(conversationId);
		if(context == null){
			context = new Context();
			context.put("currency_unit", "PLN");
			context.put("timezone", "Europe/Warsaw");
			context.put("PLN_min_quota", 2000);
			context.put("EUR_min_quota", 1000);
			context.put("GBP_min_quota", 1000);
			context.put("USD_min_quota", 1000);
			context.put("PLN_quota", 1000000);
			context.put("EUR_quota", 400000);
			context.put("GBP_quota", 400000);
			context.put("USD_quota", 500000);
		}
		options = options.newBuilder().input(data).context(context).build();
		MessageResponse response = service.message(options).execute();
		context = response.getContext();
		
		mem.put(context.get("conversation_id").toString(),context);
		return response;
	}
	
	public Context getContext(String conversationId) {
		return mem.get(conversationId);		
	}
	public void updateContext(Context context) {
		mem.put(context.get("conversation_id").toString(),context);		
	}
	@PostConstruct
	public void init() {
		Properties properties = new Properties();
		InputStream inputStream = getClass().getResourceAsStream("/META-INF/eBanking.properties");
		try {
			properties.load(inputStream);
		} catch (IOException e) {
			LOGGER.severe("Can't load eBanking.properties file");
		}
		service = new Assistant("2018-09-20");
		
		service.setUsernameAndPassword(properties.getProperty("awizo_ConversationUsername"), properties.getProperty("awizo_ConversationPassword"));
		service.setEndPoint(properties.getProperty("awizo_URL"));
		workspaceId = properties.getProperty("awizo_ConversationWorkspaceId");
		options = new MessageOptions.Builder(workspaceId).build();
	}


}