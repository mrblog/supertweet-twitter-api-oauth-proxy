package neoauth.model;

public class RowUser {
	private String date;
	private String user;
	private String userFollowers;
	private String userFriends;
	private String tweetCount;
	private String refererBody;
	private String refererURL;
	
	public RowUser(String user) {
		setUser(user);
	}
	
	public void setDate(String date) {
		this.date = date;
	}
	public String getDate() {
		return date;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getUser() {
		return user;
	}
	public void setUserFollowers(String userFollowers) {
		this.userFollowers = userFollowers;
	}
	public String getUserFollowers() {
		return userFollowers;
	}
	public void setUserFriends(String userFriends) {
		this.userFriends = userFriends;
	}
	public String getUserFriends() {
		return userFriends;
	}
	public void setTweetCount(String tweetCount) {
		this.tweetCount = tweetCount;
	}
	public String getTweetCount() {
		return tweetCount;
	}

	public void setRefererBody(String refererBody) {
		this.refererBody = refererBody;
	}

	public String getRefererBody() {
		return refererBody;
	}

	public void setRefererURL(String refererURL) {
		this.refererURL = refererURL;
	}

	public String getRefererURL() {
		return refererURL;
	}
	

}
