package neoauthlo.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.FileLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import neoauth.model.OAuthAccessToken;
import neoauth.model.ScreennameMap;
import neoauth.model.UserInfo;
import neoauthlo.util.ProxyConfig;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.StreamException;

public class DataAccessManager {

    private Logger logger = Logger.getLogger(DataAccessManager.class.getName());

    	private static String rootpath;
    	private static final long WAIT_TIME = 100L;
    	private static final long LOCK_TIMEOUT = 5000L;
	
		private static CacheManager mgr;
	    private static Cache cache;
	    
	    static {
	    	rootpath = ProxyConfig.getConfigRoot() + "/jos";
	    	mgr = new CacheManager(ProxyConfig.getConfigFile("ehcache-DataAccessManager.xml"));
	        cache = mgr.getCache("DataAccessManager");
	    }
	    
	    
		private XStream xstream;
		
		public DataAccessManager() {

			xstream = new XStream();
	
		}
		
		private File lock(String group, String key) {
			String odir = rootpath + "/locks/" + group;
		    boolean exists = (new File(odir)).exists();
		    if (!exists) {
			    boolean status;
		    	status = new File(odir).mkdirs();
		    	logger.info("lock: " + group + "/" + key + " dir " + odir + " " + (status ? "success" : "failure"));
		    }
		    File lockfile = new File(odir, key);
		    try {
				long waited = 0;
				boolean fl = false;
				while(waited <= LOCK_TIMEOUT && !(fl = lockfile.mkdir())) {
					logger.info("lock in use " + group + "/" + key + " retrying in " + WAIT_TIME + "ms");
					try {Thread.sleep(WAIT_TIME);} catch(InterruptedException ie) { logger.warning("Lock InterruptedException on " + group + "/" + key); }
					waited += WAIT_TIME;
				}
				if (!fl) {
				     // TIMEOUT!
					logger.severe("Lock Timeout on " + group + "/" + key);
					return null;
				}
				//logger.info("got lock on " + group + "/" + key);
				return lockfile;

			} catch (Exception e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

		}
	
		private void unlock(File lockfile) {
			lockfile.delete();
		}
		
		public void storeOAuthAccessToken(String id, String token, String tokenSecret) {
			
			if (id.contains("/")) {
				logger.warning("storeOAuthAccessToken: Invalid UserID: " + id);
				return;
			}
			
			OAuthAccessToken t = new OAuthAccessToken(id, token, tokenSecret);

			FileOutputStream fos;
			String odir = rootpath + "/Tokens";
		    boolean exists = (new File(odir)).exists();
		    if (!exists) {
			    boolean status;
		    	status = new File(odir).mkdir();
		    	logger.info(odir + " " + (status ? "success" : "failure"));
		    }
			String key = "/Tokens/" + id;
			try {
				fos = new FileOutputStream(odir + "/" + id);
				ObjectOutputStream oos = xstream.createObjectOutputStream(fos);

				FileLock lock = fos.getChannel().lock();
				try {
					oos.writeObject(t);
					// Flush and close the ObjectOutputStream.
					//
					oos.flush();
					cache.put(new Element(key, t));
				} finally {
					lock.release();
				}
				oos.close();
			} catch (Exception e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public OAuthAccessToken getOAuthAccessToken(String id) throws IOException, ClassNotFoundException, StreamException {
			OAuthAccessToken t = null;

			String key = "/Tokens/" + id;
			Element element = cache.get(key);
			if (element != null) {
				t = (OAuthAccessToken) element.getObjectValue();
				if (t != null) {
					//logger.info("getOAuthAccessToken cache hit, id: " + id);
					return t;
				}
			}
			
			FileInputStream fis;
			fis = new FileInputStream(rootpath + "/Tokens/" + id);

			ObjectInputStream ois;

			ois = xstream.createObjectInputStream(fis);

			t = (OAuthAccessToken) ois.readObject();

			ois.close();

			cache.put(new Element(key, t));

			return t;		
		}
	
		public void delOAuthAccessToken(String id) {

			File fx = new File(rootpath + "/Tokens/" + id);
			fx.delete();
		}
		
		public boolean existsOAuthAccessToken(String id) {

			File fx = new File(rootpath + "/Tokens/" + id);
			return fx.exists();
		}
	
	
		public UserInfo getUserInfoByScreenname(String screenname) {
			String id;
			try {
				id = getIdByScreenname(screenname);	
				return getUserInfo(id);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		public UserInfo getUserInfo(String id) {
			UserInfo u = null;

			String key = "/UserInfo/" + id;
			Element element = cache.get(key);
			if (element != null) {
				u = (UserInfo) element.getObjectValue();
				if (u != null) {
					return u;
				}
			}
			File lck = lock("UserInfo", id);
			if (lck == null)
				return null;
			try {
				FileInputStream fis;
				try {
					fis = new FileInputStream(rootpath + "/UserInfo/" + id);
				} catch (FileNotFoundException e) {
					unlock(lck);
					return null;
				}

				try {
					ObjectInputStream ois;
					ois = xstream.createObjectInputStream(fis);
					u = (UserInfo) ois.readObject();
					ois.close();
					cache.put(new Element(key, u));
				} catch (IOException e) {
					logger.log(Level.SEVERE, "getUserInfo id " + id + " IOException " + e.getMessage());
					e.printStackTrace();
					unlock(lck);
					return null;
				} catch (ClassNotFoundException e) {
					logger.log(Level.SEVERE, "getUserInfo id " + id + " ClassNotFoundException " + e.getMessage());
					e.printStackTrace();
					unlock(lck);
					return null;
				} catch (StreamException e) {
					logger.log(Level.SEVERE, "getUserInfo id " + id + " StreamException " + e.getMessage());
					e.printStackTrace();
					unlock(lck);
					return null;
				}
			} finally {
				unlock(lck);
			}
			return u;		
		}
		
		public void storeUserInfo(String id, UserInfo u) {
			
			if (id.contains("/")) {
				logger.warning("storeUserId: Invalid id: " + id);
				return;
			}
			storeScreennameMap(u.getScreenName(), id);
			File lck = lock("UserInfo", id);
			if (lck == null)
				return;
			try {
				FileOutputStream fos;
				String odir = rootpath + "/UserInfo";
				boolean exists = (new File(odir)).exists();
				if (!exists) {
					boolean status;
					status = new File(odir).mkdir();
					logger.info(odir + " " + (status ? "success" : "failure"));
					}
				try {
					fos = new FileOutputStream(odir + "/" + id);
					ObjectOutputStream oos = xstream.createObjectOutputStream(fos);

					FileLock lock = fos.getChannel().lock();
					try {
						oos.writeObject(u);
						// Flush and close the ObjectOutputStream.
						//
					oos.flush();
					} finally {
						lock.release();
					}
					oos.close();
					String key = "/UserInfo/" + id;
					cache.put(new Element(key, u));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} finally {
				unlock(lck);
			}
		}
		
		public void storeScreennameMap(String screenname, int id) {
			storeScreennameMap(screenname, id+"");
		}
	
		public void storeScreennameMap(String screenname, String id) {
			
			if (screenname.contains("/")) {
				logger.warning("storeScreennameMap: Invalid screenname: " + screenname);
				return;
			}
			
			ScreennameMap m = new ScreennameMap(screenname, id);

			FileOutputStream fos;
			String odir = rootpath + "/ScreennameMap";
		    boolean exists = (new File(odir)).exists();
		    if (!exists) {
			    boolean status;
		    	status = new File(odir).mkdir();
		    	logger.info(odir + " " + (status ? "success" : "failure"));
		    }
			String key = "/ScreennameMap/" + screenname.toLowerCase();
			try {
				fos = new FileOutputStream(odir + "/" + screenname.toLowerCase());
				ObjectOutputStream oos = xstream.createObjectOutputStream(fos);

				FileLock lock = fos.getChannel().lock();
				try {
					oos.writeObject(m);
					// Flush and close the ObjectOutputStream.
					//
					oos.flush();
					cache.put(new Element(key, m.getId()));
				} finally {
					lock.release();
				}
				oos.close();
			} catch (Exception e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public String getIdByScreenname(String screenname) throws IOException, ClassNotFoundException, StreamException, FileNotFoundException {
			ScreennameMap m = null;

			String key = "/ScreennameMap/" + screenname.toLowerCase();
			Element element = cache.get(key);
			if (element != null) {
				String id = (String) element.getObjectValue();
				if (id != null) {
					return id;
				}
			}
			FileInputStream fis;
			fis = new FileInputStream(rootpath + "/ScreennameMap/" + screenname.toLowerCase());

			ObjectInputStream ois;

			ois = xstream.createObjectInputStream(fis);

			m = (ScreennameMap) ois.readObject();

			ois.close();

			if (m != null) {
				cache.put(new Element(key, m.getId()));
				return m.getId();		
			} else
				throw new FileNotFoundException("readObject returned null");
		}

		
		public void close() {
		}

	
}