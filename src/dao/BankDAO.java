package dao;

import static com.cloudant.client.api.query.Expression.eq;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.query.QueryBuilder;
import com.cloudant.client.api.query.QueryResult;
import com.cloudant.client.api.query.Selector;
import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

@Stateless
@LocalBean
public class BankDAO {
	private CloudantClient client = null;
	private Database db = null;
	private Database holidays = null;
	private HashMap<String,String> holidaysCache = null;
	private static final String CLASS_NAME = BankDAO.class.getName();
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

	@PostConstruct
	public void init() {
		String url = null;
		String iam = null;
		try {
			InputStream inputStream = getClass().getResourceAsStream("/META-INF/eBanking.properties");
			Properties properties = new Properties();
			properties.load(inputStream);
			url = properties.getProperty("db_URL");
			iam = properties.getProperty("db_apikey");
		} catch (IOException e2) {
			LOGGER.severe("Can't load eBanking.properties file");
		}
		//try {
			String VCAP_SERVICES = System.getenv("VCAP_SERVICES");
			LOGGER.fine(VCAP_SERVICES);
			/*
			JSONObject vcap = (JSONObject) JSONObject.parse(VCAP_SERVICES);
			JSONArray cloudant = (JSONArray) vcap.get("cloudantNoSQLDB");
			JSONObject cloudantInstance = (JSONObject) cloudant.get(0);
			JSONObject cloudantCredentials = (JSONObject) cloudantInstance.get("credentials");
			url = (String) cloudantCredentials.get("url");
			iam = (String) cloudantCredentials.get("apikey");
		} catch (IOException e) {// 
			LOGGER.severe("Can't load VCAP_SERVICES");
		}*/
		try {
			client = ClientBuilder.url(new URL(url)).iamApiKey(iam).build();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		db = client.database("bankdb", false);
		holidays = client.database("holidays", false);
		holidaysCache = getHolidays();
		LOGGER.logp(Level.FINEST, CLASS_NAME, "init():", "Persisted");
	}

	// context.get("currency_value"), context.get("currency_unit"),
	// context.get("awizo_date")

	public List<String> findBranches(String city, String value, String unit, String date) {
		List<String> result = new ArrayList<String>();
		QueryResult<Branch> newtenant = db.query(new QueryBuilder(eq("zoom", 13).eq("city", city)).build(),
				Branch.class);
		result.add(newtenant.getDocs().get(0).toString());
		String selector = ", \"quota." + unit + "\": {\"$gte\": " + value + " }";

		newtenant = db.query("{\"selector\": {\"city\": { \"$eq\": \"" + city + "\" }, \"quota." + unit
				+ "\": {\"$gte\": " + value + " } },\"sort\": [{\"_id\": \"asc\"}]}", Branch.class);

		//System.out.println(newtenant.getDocs());
		for (Iterator<Branch> i = newtenant.getDocs().iterator(); i.hasNext();) {
			Branch b = i.next();
			result.add(b.toString());
		}
		return result;
	}

	public class City {
		public String city;
	}

	public List<String> findCities() {
		List<String> result = new ArrayList<String>();
		Selector selector = eq("zoom", 13);
		QueryResult<City> newtenant = db.query(new QueryBuilder(selector).build(), City.class);
		for (Iterator<City> i = newtenant.getDocs().iterator(); i.hasNext();) {
			result.add(i.next().city);
		}
		return result;
	}

	public String getBranchInfo(String text) {
		QueryResult<Branch> newtenant = db.query(new QueryBuilder(eq("_id", text)).build(),
				Branch.class);
		List<Branch> docs = newtenant.getDocs();
		if (docs.isEmpty()) return "";
		return docs.get(0).getInfo();
	}
	class Holiday{
		String date;
		String info;
	}
	private HashMap<String,String> getHolidays() {
		QueryResult<Holiday> newtenant = holidays.query(new QueryBuilder(com.cloudant.client.api.query.Expression.gt("date", "2018-11-01")).build(),
				Holiday.class);
		List<Holiday> docs = newtenant.getDocs();
		HashMap<String,String> result = new HashMap<String,String>();
		Holiday h = null;
		for (Iterator<Holiday> i = newtenant.getDocs().iterator(); i.hasNext();) {
			h=i.next();
			result.put(h.date, h.info);
		}
		return result;
	}
	public String isHoliday(String date){
		return holidaysCache.get(date);
	}
}
