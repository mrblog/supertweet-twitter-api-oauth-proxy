package neoauth.model;

public class RowAPIEvent {
	private String Date;
	private String User;
	private String Method;
	private String Request;
	private String Fields;
	
	public void setDate(String date) {
		Date = date;
	}
	public String getDate() {
		return Date;
	}
	public void setUser(String user) {
		User = user;
	}
	public String getUser() {
		return User;
	}
	public void setMethod(String method) {
		Method = method;
	}
	public String getMethod() {
		return Method;
	}
	public void setRequest(String request) {
		Request = request;
	}
	public String getRequest() {
		return Request;
	}
	public void setFields(String fields) {
		Fields = fields;
	}
	public String getFields() {
		return Fields;
	}
	
	

}