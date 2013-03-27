package neoauthlo.resource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import neoauthlo.NeoAuthApplication;
import neoauth.model.OAuthAccessToken;
import neoauth.model.UserInfo;
import neoauthlo.model.DataAccessManager;
import neoauthlo.util.oAuthConfig;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import com.Ostermiller.util.Base64;
import com.Ostermiller.util.ExcelCSVPrinter;

public class ProxyResource extends BaseResource {
	
	private final static Logger logger = Logger.getLogger(ProxyResource.class.getName());

	protected Representation authResult;
	
	protected UserInfo u;
	
	private OAuthAccessToken aToken;

	private oAuthConfig oAuthCfg;
	
    public ProxyResource() {
    }

    @Override
    protected void doInit() {
	    oAuthCfg = (oAuthConfig) getContextAttribute("oauth.config");
    }

	private synchronized void logAPI(String method, String req, final Map<String, String> fields) {
		// date, user, id, method, request, fields
		File f = new File(NeoAuthApplication.logPath + "/apilog.csv");
		ExcelCSVPrinter ep = null;
		FileWriter fw = null;
		try {
			fw = new FileWriter(f, true);
			ep = new ExcelCSVPrinter(fw);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			ep.write(sdf.format(new Date()));
			ep.write(u.getScreenName());
			ep.write(method);
			ep.write(req);
			ep.write(fields.toString());
			ep.writeln();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (ep != null)
				try {
					ep.close();
				} catch (IOException e) {
					// ignore
				}
			if (fw != null)
				try {
					fw.close();
				} catch (IOException e) {
					// ignore
				}
		}
	}
	
	private synchronized void logError(String req, int code, String errmsg) {
		// date, user, request, code, errmsg
		File f = new File(NeoAuthApplication.logPath + "/errors.csv");
		ExcelCSVPrinter ep = null;
		FileWriter fw = null;
		try {
			fw = new FileWriter(f, true);
			ep = new ExcelCSVPrinter(fw);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			ep.write(sdf.format(new Date()));
			if (u != null) {
				ep.write(u.getScreenName());
			} else {
				ep.write("null");
			}
			ep.write(req);
			ep.write(code+"");
			ep.write(errmsg);
			ep.writeln();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (ep != null)
				try {
					ep.close();
				} catch (IOException e) {
					// ignore
				}
			if (fw != null)
				try {
					fw.close();
				} catch (IOException e) {
					// ignore
				}
		}
	}
	
    private HttpParams getParams() {
        // Tweak further as needed for your app
        HttpParams params = new BasicHttpParams();
        // set this to false, or else you'll get an
        // Expectation Failed: error
        HttpProtocolParams.setUseExpectContinue(params, false);
        return params;
    }
    
	private void getToken(DataAccessManager dm, String id) {
		aToken = null;
	    try {
	    	aToken = dm.getOAuthAccessToken(id);
	    } catch (IOException e) {
	    	// TODO Auto-generated catch block
	    	logger.warning("isValidUserId IOException: " + e);
	    	e.printStackTrace();
	    	return;
	    } catch (Exception e) {
	    	// TODO Auto-generated catch block
	    	logger.warning("isValidUserId Exception: " + e);
	    	e.printStackTrace();
	    	return;
	    }
	}
	
	protected boolean isAuthOk() {

		ChallengeResponse x = getRequest().getChallengeResponse();
		if (x != null) {
			ChallengeScheme scheme = x.getScheme();
			//logger.info("Scheme: " + scheme.getName());
			if (!scheme.equals(ChallengeScheme.HTTP_BASIC)) {
				logger.warning("NOT BASIC AUTH!");
				ChallengeRequest authentication = new ChallengeRequest(ChallengeScheme.HTTP_BASIC, "SuperTweet.net API");
		        getResponse().getChallengeRequests().add(authentication);
	    		setAuthResult(errorRepresentation(Status.CLIENT_ERROR_NOT_ACCEPTABLE, "BASIC Authentication required"));
	    		return false;
			}
			String creds = x.getCredentials();
			//logger.info("Credentials: " + creds);
			String dcreds = Base64.decode(creds);
			//logger.info("Decoded Credentials: " + dcreds);
			String[] temp = dcreds.split(":");
			if (temp.length != 2) {
				//logger.warning("Invalid Credential format: " + dcreds);
	    		setAuthResult(errorRepresentation(Status.CLIENT_ERROR_NOT_ACCEPTABLE, "Invalid Credential format"));
	    		return false;
			}
			String username = temp[0];
			String passwd = temp[1];
			DataAccessManager dm = new DataAccessManager();
			u = dm.getUserInfoByScreenname(username);
			if (u == null || u.getBasicAuth() == null ) {
				logger.warning("no such user: " + username);
	    		setAuthResult(errorRepresentation(Status.CLIENT_ERROR_UNAUTHORIZED, "user '" + username + "' unknown"));
	    		dm.close();
	    		return false;
			}
			if (!u.getBasicAuth().equals(passwd)) {
				//logger.warning("invalid password '" + passwd + "' for username: " + username);
	    		setAuthResult(errorRepresentation(Status.CLIENT_ERROR_UNAUTHORIZED, "invalid username/password"));
	    		dm.close();
	    		return false;
			}
			if (u.isEnabledFeature("blocked")) {
				logger.warning("blocked username: " + username);
	    		setAuthResult(errorRepresentation(Status.CLIENT_ERROR_FORBIDDEN, "blocked"));
	    		dm.close();
	    		return false;
			}
			getToken(dm, u.getId());

		    if (aToken == null) {
				logger.warning("No AuthToken for username: " + username);
	    		setAuthResult(errorRepresentation(Status.CLIENT_ERROR_UNAUTHORIZED, "missing/invalid OAuth"));
	    		dm.close();
	    		return false;
		    }
    		dm.close();
		    return true;
		} else {
			logger.info("No Authentication, requesting via WWW-Authenticate");
			ChallengeRequest authentication = new ChallengeRequest(ChallengeScheme.HTTP_BASIC, "SuperTweet.net API");
	        getResponse().getChallengeRequests().add(authentication);
    		setAuthResult(errorRepresentation(Status.CLIENT_ERROR_UNAUTHORIZED, "Authentication required"));
    		return false;
		}
	}

	protected void setAuthResult(Representation authResult) {
		this.authResult = authResult;
	}

	protected Representation getAuthResult() {
		return authResult;
	}

	protected Representation errorRepresentation(int code, String errmsg) {
		return errorRepresentation(new Status(code), errmsg);
	}
	
	protected Representation errorRepresentation(Status status, String errmsg) {
		if (status.getCode() != 200) 
			getResponse().setStatus(status);
		String req = getRequest().getResourceRef().getPath();
		logError(req, status.getCode(), errmsg);
		return new StringRepresentation("{\"request\":\"" + req + "\",\"error\":\"" + errmsg + "\"}", MediaType.APPLICATION_JSON);
	}
	
	private static UrlEncodedFormEntity mapToFormEntity(final Map<String, String> fields) throws UnsupportedEncodingException {
		final ArrayList<NameValuePair> values = new ArrayList<NameValuePair>(fields.size());
		for (final Entry<String, String> entry : fields.entrySet()) {
			values.add(new BasicNameValuePair(entry.getKey(), (String) entry.getValue()));
		}
	
	    final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(values, HTTP.UTF_8);
	    return entity;
	}

	protected Representation doGetRequest(final Map<String, String> fields) {
		return doRequest("GET", fields);
	}

	protected Representation doDeleteRequest(final Map<String, String> fields) {
		return doRequest("DELETE", fields);
	}
	
	protected Representation doRequest(String method, final Map<String, String> fields) {
		String req = getRequest().getResourceRef().getPath();
		if (!req.endsWith(".json")) {
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return new StringRepresentation("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<errors>\n  <error code=\"34\">Sorry, that page does not exist</error>\n</errors>", MediaType.APPLICATION_XML);
		}
		if (!isAuthOk()) {
			return authResult;
		}
		logAPI(method, req, fields);
		OAuthConsumer consumer = new DefaultOAuthConsumer(oAuthCfg.getConsumer_key(),
    		oAuthCfg.getConsumer_secret());
		consumer.setTokenWithSecret(aToken.getToken(),
            aToken.getTokenSecret());
		StringBuffer urlStr = new StringBuffer("http://api.twitter.com");
		urlStr.append(req);
		String d = "?";
		for (final Entry<String, String> entry : fields.entrySet()) {
			urlStr.append(d);
			urlStr.append(entry.getKey());
			urlStr.append("=");
			try {
				urlStr.append(URLEncoder.encode((String) entry.getValue(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			d = "&";
		}
		
		URL url;
		HttpURLConnection request = null;
		try {
			url = new URL(urlStr.toString());
			logger.info(method + " " + url.toString());
			request = (HttpURLConnection) url.openConnection();

			if (!method.equals("GET"))  {
				request.setRequestMethod(method);
				request.setDoOutput(true);
			}
			// sign the request
			consumer.sign(request);

			// send the request
			request.connect();

			// response status should be 200 OK
			int statusCode = request.getResponseCode();
			Form responseHeaders = (Form) getResponse().getAttributes().get("org.restlet.http.headers");
            if (responseHeaders == null) {
                responseHeaders = new Form();
                getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
            }
            responseHeaders.add("X-TwitterAPI-Status", statusCode+"");

			if (statusCode != 200) {
				logger.info("Twitter statusCode:" + statusCode + " on request " + req);
				logError(req, statusCode, "Twitter API");
				getResponse().setStatus(new Status(statusCode));
				InputStream is = request.getErrorStream();
				final String data = IOUtils.toString(is);
				return new StringRepresentation(data, MediaType.APPLICATION_JSON);
			} else {
				InputStream is = request.getInputStream();
				final String data = IOUtils.toString(is);	
				return new StringRepresentation(data, MediaType.APPLICATION_JSON);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return errorRepresentation(Status.SERVER_ERROR_INTERNAL, e.getMessage());

		} finally {
			//close the connection, set all objects to null
			if (request != null)
					request.disconnect();
			request = null;
		}
	}
	
	protected Representation doPostRequest(final Map<String, String> fields) {
		String req = getRequest().getResourceRef().getPath();
		if (!req.endsWith(".json")) {
            return new StringRepresentation("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<errors>\n  <error code=\"34\">Sorry, that page does not exist</error>\n</errors>", MediaType.APPLICATION_XML);
		}
		if (!isAuthOk()) {
			return authResult;
		}
	
		logAPI("POST", req, fields);

		// Create a new consumer using the commons implementation
		OAuthConsumer consumer = new CommonsHttpOAuthConsumer(oAuthCfg.getConsumer_key(),
    		oAuthCfg.getConsumer_secret());
		consumer.setTokenWithSecret(aToken.getToken(),
            aToken.getTokenSecret());
		HttpPost apiPost = new HttpPost(
    		"http://api.twitter.com" + req);

		UrlEncodedFormEntity requestEntity;
		try {
			requestEntity = mapToFormEntity(fields);

			apiPost.setEntity(requestEntity);
    
			// The body of a multi-part post isn't needed
			// for the generation of the signature
			consumer.sign(apiPost);

    
			DefaultHttpClient httpClient = new DefaultHttpClient(getParams());

			// If you're interested in the headers,
			// implement and add a request interceptor that prints them
			//httpClient.addRequestInterceptor(new PrintRequestInterceptor());


			final org.apache.http.HttpResponse response = httpClient.execute(apiPost);
			final HttpEntity responseEntity = response.getEntity();
			InputStream is = responseEntity.getContent();

			final String data = IOUtils.toString(is);
			StatusLine st = response.getStatusLine();
			//logger.info("getStatusLine: " + st);
			int c = st.getStatusCode();
			Form responseHeaders = (Form) getResponse().getAttributes().get("org.restlet.http.headers");
            if (responseHeaders == null) {
                responseHeaders = new Form();
                getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
            }
            responseHeaders.add("X-TwitterAPI-Status", c+"");
			if (c != 200) {
				getResponse().setStatus(new Status(c));
				logError(req, c, "Twitter API:" + st.getReasonPhrase() + data.replaceAll("\n", "") + " fields: " + fields);
			}
			return new StringRepresentation(data, MediaType.APPLICATION_JSON);
		} catch (Exception e) {
			e.printStackTrace();
			return errorRepresentation(Status.SERVER_ERROR_INTERNAL, e.getMessage());
		}
	
	}
	
	
}

