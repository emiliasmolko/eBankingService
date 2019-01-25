package dao;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import com.ibm.watson.developer_cloud.language_translator.v3.LanguageTranslator;
import com.ibm.watson.developer_cloud.language_translator.v3.model.CreateModelOptions;
import com.ibm.watson.developer_cloud.language_translator.v3.model.TranslateOptions;
import com.ibm.watson.developer_cloud.language_translator.v3.model.Translation;
import com.ibm.watson.developer_cloud.language_translator.v3.model.TranslationModel;
import com.ibm.watson.developer_cloud.language_translator.v3.model.TranslationResult;
import com.ibm.watson.developer_cloud.service.security.IamOptions;

@Stateless
@LocalBean
public class Translate {
	private TranslateOptions.Builder translateOptions = null;
	private LanguageTranslator service = null;
	private static final Logger LOGGER = Logger.getLogger(Translate.class.getName());
	
	public String translate1(String text) {
		List <String> input = new ArrayList<String>();
		input.add(text);
		TranslationResult result = service.translate(translateOptions.text(input).build()).execute();
		List<Translation> lt = result.getTranslations();
		String t = "";
		for (Iterator<Translation> i = lt.iterator();i.hasNext();) {
			t += i.next().getTranslationOutput();
		}
		return t;
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
		IamOptions options = new IamOptions.Builder().apiKey(properties.getProperty("Translate_Key")).build();
		service = new LanguageTranslator("2018-05-01", options);
		service.setEndPoint(properties.getProperty("Translate_URL"));
		
		
		/*File glossary = new File("glossary.tmx");
		CreateModelOptions createModelOptions = new CreateModelOptions.Builder()
		  .baseModelId("pl-en")
		  .name("custom-pl-en")
		  .forcedGlossary(glossary)
		  .build();

		TranslationModel model = service.createModel(createModelOptions).execute();
		*/
		translateOptions = new TranslateOptions.Builder().modelId("pl-en");
		
		
	}


}