package dao;

public class Branch {
	  private String _id = "1";
	  private String info;
	  private String address;
	  private String name;
	  private String city;
	  private String lat;
	  private String lng;
	  private OperatingHours opened;
	  

	  public String toString() {
	    String text =  "{ \"id\": " + _id;
	    if (address != null) { 
	    	text +=",\n\"address\": \"" + address+"\"";
	    }
	    if (name != null) { 
	    	text +=	",\n\"name\": \"" + name+"\"";
	    }
	    if(info!= null) {
	    	text +=	",\n\"info\": \"" + info+"\"";
	    }
	    if(city!= null) {
	    	text +=	",\n\"city\": \"" + city+"\"";
	    }
	    if(lat!= null) {
	    	text +=	",\n\"lat\": " + lat;
	    }
	    if(lng!= null) {
	    	text +=	",\n\"lng\": " + lng;
	    }
	    if(opened!= null) {
	    	text +=	",\n\"opened\": " + opened.toString();
	    }
	    text +=	"\n}";
	    return text;
	  }
	  public String getInfo() {
		  return name+", "+address;
	  }
}