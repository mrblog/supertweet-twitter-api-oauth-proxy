package neoauth.model;

public class OAuthAccessToken {
	private String UserId;
	private String token;
	private String tokenSecret;
	
	public OAuthAccessToken(String UserId, String token, String tokenSecret) {
		this.UserId = UserId;
		this.token = token;
		this.tokenSecret = tokenSecret;
	}

	public void setToken(String token) {
		this.token = token;
	}
	public String getToken() {
		return token;
	}
	public void setTokenSecret(String tokenSecret) {
		this.tokenSecret = tokenSecret;
	}
	public String getTokenSecret() {
		return tokenSecret;
	}
	public void setUserId(String userId) {
		UserId = userId;
	}
	public String getUserId() {
		return UserId;
	}
	
}
