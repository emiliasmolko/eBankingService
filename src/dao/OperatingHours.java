package dao;

public class OperatingHours {
	private int monfri_start=-1;
	private int monfri_stop=-1;
	private int sat_start=-1;
	private int sat_stop=-1;
	private int sun_start=-1;
	private int sun_stop=-1;
	public String getInfo() {
		String result = "";
		if (monfri_start>-1) {
			result+="poniedziałek-piątek : "+monfri_start+":00 - "+monfri_stop+":00";
		}
		if (sat_start>-1) {
			result+=", sobota : "+sat_start+":00 - "+sat_stop+":00";
		}
		if (sun_start>-1) {
			result+=", niedziela : "+sun_start+":00 - "+sun_stop+":00";
		}
		return result;
	}
	public String toString() {
	    return "{ \"monfri_start\": " + monfri_start + ",\n\"monfri_stop\": " + monfri_stop + 
	    		",\n\"sat_start\": " + sat_start + 
	    		",\n\"sat_stop\": " + sat_stop + 
	    		",\n\"sun_start\": " + sun_start + 
	    		",\n\"sun_stop\": " + sun_stop +
	    		",\n\"info\": \"" + getInfo() +
	    		"\"\n}";
	  }
}
