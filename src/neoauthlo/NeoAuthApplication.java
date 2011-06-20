package neoauthlo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import neoauthlo.resource.GeneralAPIProxyResource;
import neoauthlo.resource.SetupAccessResource;
import neoauthlo.util.oAuthConfig;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.MapVerifier;


public class NeoAuthApplication extends Application {
	private final Logger logger = Logger.getLogger(NeoAuthApplication.class.getName());

   
    public static String logPath;
        
    public static int MIN_DESC_SIZE = 25;
        
    public NeoAuthApplication() {
    }


    @Override
    public void stop() {
    	
    	try {
			super.stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }

    @Override
    public void start() throws Exception {

        // todo:  we might consider moving all this to the public no-arg constructor above.
        logger.info("neoauth Restlet startup");
        
        Map<String, Object> attrs = getContext().getAttributes();

        logPath = System.getProperty("user.home") + "/.supertweet/logs";

        attrs.put("oauth.config", new oAuthConfig());

        super.start();
    }
    
    /**
     * Creates a root Restlet that will receive all incoming calls.
     */

    @Override
    public synchronized Restlet createRoot() {
        // Create a router Restlet that routes each call to a
        // new instance of  a given Resource.
        Router router = new Router(getContext());

        
        router.attach("/statuses/update", GeneralAPIProxyResource.class);
        router.attach("/statuses/home_timeline", GeneralAPIProxyResource.class);

        router.attach("/1/statuses/home_timeline", GeneralAPIProxyResource.class);
        router.attach("/1/statuses/user_timeline", GeneralAPIProxyResource.class);
        router.attach("/1/statuses/public_timeline ", GeneralAPIProxyResource.class);
        router.attach("/1/statuses/user_timeline ", GeneralAPIProxyResource.class);
        router.attach("/1/statuses/mentions", GeneralAPIProxyResource.class);
        router.attach("/1/statuses/retweeted_by_me", GeneralAPIProxyResource.class);
        router.attach("/1/statuses/retweeted_to_me", GeneralAPIProxyResource.class);
        router.attach("/1/statuses/retweets_of_me", GeneralAPIProxyResource.class);
        
        router.attach("/1/statuses/retweet/{id}", GeneralAPIProxyResource.class);
        router.attach("/1/statuses/show/{id}", GeneralAPIProxyResource.class);
        router.attach("/1/statuses/retweets/{id}", GeneralAPIProxyResource.class);
        router.attach("/1/statuses/{id}/retweeted_by", GeneralAPIProxyResource.class);
        router.attach("/1/statuses/{id}/retweeted_by/ids", GeneralAPIProxyResource.class);
        
        router.attach("/1/users/show", GeneralAPIProxyResource.class);
        router.attach("/1/users/lookup", GeneralAPIProxyResource.class);
        router.attach("/1/users/search", GeneralAPIProxyResource.class);
        router.attach("/1/users/suggestions", GeneralAPIProxyResource.class);
        router.attach("/1/users/suggestions/{slug}", GeneralAPIProxyResource.class);
        router.attach("/1/statuses/friends", GeneralAPIProxyResource.class);
        router.attach("/1/statuses/friends/{id}", GeneralAPIProxyResource.class);
        router.attach("/1/statuses/followers", GeneralAPIProxyResource.class);
        
        router.attach("/1/{user}/lists", GeneralAPIProxyResource.class);
        router.attach("/1/{user}/lists/{id}", GeneralAPIProxyResource.class);
        router.attach("/1/{user}/lists/memberships", GeneralAPIProxyResource.class);
        router.attach("/1/{user}/lists/subscriptions", GeneralAPIProxyResource.class);
        
        router.attach("/1/{user}/{list_id}/members", GeneralAPIProxyResource.class);
        router.attach("/1/{user}/{list_id}/subscribers", GeneralAPIProxyResource.class);
        router.attach("/1/{user}/{list_id}/members/{id}", GeneralAPIProxyResource.class);
        router.attach("/1/{user}/{list_id}/subscribers/{id}", GeneralAPIProxyResource.class);
        
        router.attach("/1/direct_messages", GeneralAPIProxyResource.class);
        router.attach("/1/direct_messages/sent", GeneralAPIProxyResource.class);
        router.attach("/1/direct_messages/new", GeneralAPIProxyResource.class);
        router.attach("/1/direct_messages/destroy/{id}", GeneralAPIProxyResource.class);
        
        router.attach("/1/friendships/create/{id}", GeneralAPIProxyResource.class);
        router.attach("/1/friendships/destroy/{id}", GeneralAPIProxyResource.class);
        router.attach("/1/friendships/exists", GeneralAPIProxyResource.class);
        router.attach("/1/friendships/show", GeneralAPIProxyResource.class);
        router.attach("/1/friendships/incoming", GeneralAPIProxyResource.class);
        router.attach("/1/friendships/outgoing", GeneralAPIProxyResource.class);
        
        router.attach("/1/friends/ids", GeneralAPIProxyResource.class);
        router.attach("/1/followers/ids", GeneralAPIProxyResource.class);
        
        router.attach("/1/account/verify_credentials", GeneralAPIProxyResource.class);
        router.attach("/1/account/rate_limit_status", GeneralAPIProxyResource.class);
        router.attach("/1/account/update_delivery_device", GeneralAPIProxyResource.class);
        router.attach("/1/account/update_profile_colors", GeneralAPIProxyResource.class);
        router.attach("/1/account/update_profile", GeneralAPIProxyResource.class);

        router.attach("/1/favorites", GeneralAPIProxyResource.class);
        router.attach("/1/favorites/create/{id}", GeneralAPIProxyResource.class);
        router.attach("/1/favorites/destroy/{id}", GeneralAPIProxyResource.class);
       
        router.attach("/1/notifications/follow/{id}", GeneralAPIProxyResource.class);
        router.attach("/1/notifications/leave/{id}", GeneralAPIProxyResource.class);

        router.attach("/1/blocks/create/{id}", GeneralAPIProxyResource.class);
        router.attach("/1/blocks/destroy/{id}", GeneralAPIProxyResource.class);
        router.attach("/1/blocks/exists/{id}", GeneralAPIProxyResource.class);
        router.attach("/1/blocks/blocking", GeneralAPIProxyResource.class);
        router.attach("/1/blocks/blocking/ids", GeneralAPIProxyResource.class);
 
        router.attach("/1/report_spam", GeneralAPIProxyResource.class);
  
        router.attach("/1/saved_searches", GeneralAPIProxyResource.class);
        router.attach("/1/saved_searches/show/{id}", GeneralAPIProxyResource.class);
        router.attach("/1/saved_searches/create", GeneralAPIProxyResource.class);
        router.attach("/1/saved_searches/destroy/{id}", GeneralAPIProxyResource.class);

        router.attach("/1/geo/nearby_places", GeneralAPIProxyResource.class);
 
        router.attach("/1/statuses/update", GeneralAPIProxyResource.class);
        router.attach("/1/statuses/destroy/{id}", GeneralAPIProxyResource.class);

        // Protected /setup routes
        Router srouter = new Router(getContext());
        srouter.attach("/access", SetupAccessResource.class);

        // Create a simple password verifier  
        MapVerifier verifier = new MapVerifier();  
        loadUsers(verifier);
        
        // Create a Guard  
        ChallengeAuthenticator sguard = new ChallengeAuthenticator(getContext(), ChallengeScheme.HTTP_BASIC, "SuperTweet Admin");  
        sguard.setVerifier(verifier);  
        sguard.setNext(srouter);
        router.attach("/setup", sguard);
        
        router.attachDefault(GeneralAPIProxyResource.class);

        return router;
    }
    
    private void loadUsers(MapVerifier verifier) {
    	FileReader fr;
        try {
                fr = new FileReader(System.getProperty("user.home") + "/.supertweet/adminusers");
        } catch (FileNotFoundException e) {
                // no file, use hard-coded defaults
                logger.info(e.getMessage());
                verifier.getSecrets().put("admin", "admin".toCharArray());
                return;
        }
        BufferedReader br = new BufferedReader(fr);
        String s;

        String [] temp = null;
        try {
			while ((s = br.readLine()) != null) {
				temp = s.split(":");
				if (temp.length == 2) { 
					verifier.getSecrets().put(temp[0], temp[1].toCharArray());
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
}
