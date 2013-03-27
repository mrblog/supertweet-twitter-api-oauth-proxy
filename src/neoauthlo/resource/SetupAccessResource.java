package neoauthlo.resource;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import neoauth.model.UserInfo;
import neoauthlo.model.DataAccessManager;
import neoauthlo.util.ProxyConfig;
import neoauthlo.util.oAuthConfig;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.RequestToken;
import twitter4j.auth.AccessToken;

public class SetupAccessResource extends SetupResource {

	private final static Logger logger = Logger.getLogger(SetupAccessResource.class.getName());
	
    @Get
    public Representation represent() {
    	setTitle("Access");
    	oAuthConfig oa = new oAuthConfig();
        String result = null;
        if (oa.getConsumer_key() == null) {
        	result = errorHTML("Please configure your application consumer key and secret in " + ProxyConfig.getConfigFile(ProxyConfig.APP_PROPS));
        } else {
        	Twitter twitter = new TwitterFactory().getInstance();
        	twitter.setOAuthConsumer(oa.getConsumer_key(), oa.getConsumer_secret());
        	RequestToken requestToken;
        	try {
        		requestToken = twitter.getOAuthRequestToken();
        		StringBuffer sb = new StringBuffer();
        		sb.append("<h2>Grant Access</h2><p>Open the following URL and grant access to your account:</p><p><a href=\"");
        		sb.append(requestToken.getAuthorizationURL());
        		sb.append("\" target=\"_blank\">");
        		sb.append(requestToken.getAuthorizationURL());
        		sb.append("</a></p><p><form method=\"post\"><input name=\"token\" type=\"hidden\" value=\"");
        		sb.append(requestToken.getToken());
        		sb.append("\" /><input name=\"secret\" type=\"hidden\" value=\"");
        		sb.append(requestToken.getTokenSecret());
        		sb.append("\" />Enter the PIN reported by Twitter: <input name=\"pin\" type=\"text\" size=\"40\"/><br/>Desired SuperTweet Password: <input name=\"passwd\" type=\"text\" size=\"15\" /><br/><input type=\"submit\" value=\"Submit\" /></form>");
        		result = htmlWrapper(sb.toString());
        	} catch (TwitterException e) {
        		result = errorHTML(e.getMessage() + " - check the settings in " + ProxyConfig.getConfigFile(ProxyConfig.APP_PROPS));
        	}
        }
        return new StringRepresentation(result, MediaType.TEXT_HTML);
    }
    
    private Representation doPostRequest(Map<String, String> fields) {
        
        final String token = fields.get("token");
        final String tokenSecret = fields.get("secret");
        if (token == null || tokenSecret == null) {
    		return new StringRepresentation(errorHTML("token required"), MediaType.TEXT_HTML);
        }
        RequestToken requestToken = new RequestToken(token, tokenSecret);
    	String pin = fields.get("pin");
    	logger.info("token: " + token + " secret: " + tokenSecret + " pin: " + pin);
    	if (pin != null) {
    		if (pin.length() > 0) {
    	    	oAuthConfig oa = new oAuthConfig();
    	    	Twitter twitter = new TwitterFactory().getInstance();
    	        twitter.setOAuthConsumer(oa.getConsumer_key(), oa.getConsumer_secret());
    	        AccessToken accessToken = null;
    	        User user = null;
    	        try {
					accessToken = twitter.getOAuthAccessToken(requestToken, pin);
					user = twitter.verifyCredentials();
				} catch (TwitterException e) {
		    		return new StringRepresentation(errorHTML(e.getMessage()), MediaType.TEXT_HTML);
				}
			    //persist the accessToken for future reference.
				logger.info("saving access token");
				DataAccessManager dm = new DataAccessManager();
				long id = accessToken.getUserId();
				logger.info("accessToken.getUserId()=" + id);
				//boolean firstTimeUser = !dm.existsOAuthAccessToken(id+"");
				dm.storeOAuthAccessToken(id+"", accessToken.getToken(), accessToken.getTokenSecret());
				String screenName = user.getScreenName();
				logger.info("screenName: " + screenName);
				dm.storeScreennameMap(screenName, id+"");
		        final String passwd = fields.get("passwd");
		        UserInfo u = dm.getUserInfoByScreenname(screenName);
	        	String sid = user.getId()+"";
	        	if (u == null) {
	        		u = new UserInfo(sid);
	        		u.setScreenName(screenName);
	        	}
	        	u.setBasicAuth(passwd);
	        	dm.storeUserInfo(sid, u);
				
				return new StringRepresentation(htmlWrapper("<h2>Success</h2><p>Credentials for <b>" + screenName + "</b> saved</p>"), MediaType.TEXT_HTML);

    		}
    	}
		return new StringRepresentation(errorHTML("pin required"), MediaType.TEXT_HTML);

    }
    
    @Post
	public Representation acceptRepresentation(Representation entity)
			throws ResourceException {
    	Map<String, String> fields = new HashMap<String, String>();
    	setTitle("Access");

    	Representation result = null;
		try {
			if (entity != null) {
				String data = entity.getText();
				//logPOST(data);
				if (entity.getMediaType().equals(MediaType.APPLICATION_WWW_FORM,
						true)) {
					if (data != null) {
						Form form = new Form(data);
						//logger.info("acceptRepresentation form: " + form);
						if (form != null) {
							fields = form.getValuesMap();
							//logger.info("fields: " + fields);
						}
					}
					result = doPostRequest(fields);
				} else if (entity.getMediaType().equals(MediaType.MULTIPART_FORM_DATA, true)) {
					//FileItemFactory factory = new DiskFileItemFactory();
					//RestletFileUpload fileUpload = new RestletFileUpload(factory);
					logger.warning("MULTIPART_FORM_DATA");
					result = errorRepresentation(Status.CLIENT_ERROR_BAD_REQUEST, "MULTIPART_FORM_DATA not supported yet");
				} else {
					result = errorRepresentation(Status.CLIENT_ERROR_BAD_REQUEST, "Not Form Data");
				}
			} else {
				result = doPostRequest(fields);
			}
		} catch (Exception e) {
			result = errorRepresentation(Status.SERVER_ERROR_INTERNAL, "Internal error: " + e.getMessage());
		}
		return result;
    }
}
