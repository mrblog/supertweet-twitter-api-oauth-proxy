package neoauthlo.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public class oAuthConfig {

	private final  Logger logger = Logger.getLogger(oAuthConfig.class.getName());
	
	private String Consumer_key = null;
	private String Consumer_secret = null;

    public oAuthConfig() {
    	initCreds();
    }

    private  void loadCreds() {
    	final String pfile = ProxyConfig.getConfigFile(ProxyConfig.APP_PROPS);
		logger.info("Retrieving App creds from " + pfile);
		Properties appProps = new Properties();
		FileInputStream in;
		try {
			in = new FileInputStream(pfile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		try {
			appProps.load(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		finally {
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Consumer_key = appProps.getProperty("consumer_key");
		Consumer_secret = appProps.getProperty("consumer_secret");
		
    }
	
	
	private  synchronized void initCreds() {
		if (Consumer_key == null) {
			loadCreds();
		}
	}
	
	public  String getConsumer_key() {
		initCreds();
		return Consumer_key;
	}
	
	public  String getConsumer_secret() {
		initCreds();
		return Consumer_secret;
		
	}
}
