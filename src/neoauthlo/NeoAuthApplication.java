package neoauthlo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import neoauthlo.resource.GeneralAPIProxyResource;
import neoauthlo.resource.SetupAccessResource;
import neoauthlo.util.ProxyConfig;
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

        logPath = ProxyConfig.getConfigRoot() + "/logs";

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

        router.attach("/1.1/", GeneralAPIProxyResource.class).getTemplate().setMatchingMode(org.restlet.util.Template.MODE_STARTS_WITH);;

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
                fr = new FileReader(ProxyConfig.getConfigFile("adminusers"));
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
