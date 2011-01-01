package neoauth.model;

public class ScreennameMap {

	private String Screenname;
	private String id;

	public ScreennameMap(String screenname, String id) {
		setScreenname(screenname);
		setId(id);
	}
	public ScreennameMap(String screenname, int id) {
		setScreenname(screenname);
		setId(id+"");
	}
	public void setScreenname(String screenname) {
		Screenname = screenname;
	}

	public String getScreenname() {
		return Screenname;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
	
}
