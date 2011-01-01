package neoauth.model;

public class PersistentSession {
	private String userid;

	public PersistentSession(String userid) {
		this.userid = userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getUserid() {
		return userid;
	}
	
}
