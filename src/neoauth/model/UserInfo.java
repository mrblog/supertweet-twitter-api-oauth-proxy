package neoauth.model;

import java.util.ArrayList;
import java.util.List;

public class UserInfo {
	private String id;
	private String screenName;
	private List<String>features;
	private String basicAuth;
	
	public UserInfo(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public String getScreenName() {
		return screenName;
	}
	
	public List<String>getFeatures() {
		return features;
	}
	
	public void addFeature(String f) {
		if (features == null)
			features = new ArrayList<String>();
		else
			removeFeature(f);
		features.add(f);
	}
	
	public void removeFeature(String f) {
		if (features != null && features.contains(f)) 		
			features.remove(f);
	}
	
	public boolean isEnabledFeature(String f) {
		return (features != null && features.contains(f));
	}

	public void setBasicAuth(String basicAuth) {
		this.basicAuth = basicAuth;
	}

	public String getBasicAuth() {
		return basicAuth;
	}

}
