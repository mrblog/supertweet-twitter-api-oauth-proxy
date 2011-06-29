package neoauthlo.util;

import java.io.File;

public class ProxyConfig {

	public static final String APP_PROPS = "App.properties";
	
	/*
	 * This code exists to deal with Windows where $HOME/.supertweet
	 * doesn't always work
	 * 
	 * So if there is no $HOME/.supertweet dir we look in some
	 * alternative places
	 * 
	 * Starting with the value in "proxy.rootPath" property if set
	 * with -Dproxy.rootPath= on the command line
	 * 
	 * If not set, try $HOME/supertweet and /supertweet
	 * 
	 * NOTE: there is no good way to get env vars on windows
	 * so we can't resort to that here either
	 */
	public static String getConfigRoot() {
		String p = System.getProperty("proxy.rootPath");
		if (p != null)
			return p;
		p = System.getProperty("user.home") + "/.supertweet";
		File f = new File(p);;
		if (f.exists())
			return p;
		String a = System.getProperty("user.home") + "/supertweet";
		f = new File(a);;
		if (f.exists())
			return a;
		a = "/supertweet";
		f = new File(a);;
		if (f.exists())
			return a;
		// return the default anyway, if nothing else set
		return p;
	}
	
	public static String getConfigFile(String fname) {
		return getConfigRoot() + "/" + fname;
	}
	
}
