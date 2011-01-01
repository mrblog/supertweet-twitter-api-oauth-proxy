package neoauthlo.servlet;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.logging.Logger;

/**
 * @author Mark Petrovic
 */
public class RateLimitFilter implements Filter {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(RateLimitFilter.class
			.getName());
	
	private CacheManager mgr;
    private Cache cache;

    
    public void init(FilterConfig filterConfig) throws ServletException {
    	mgr = new CacheManager(System.getProperty("user.home") + "/.supertweet/ehcache-RateLimitFilter.xml");
        cache = mgr.getCache("rateLimit");
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain)
            throws IOException, ServletException {
        String remoteAddr = req.getRemoteAddr();
        String path = ((HttpServletRequest) req).getPathInfo();
        if (!exempt(path)) {
            String key = new StringBuilder(remoteAddr).append(":").append(path).toString();
            //log.info("RateLimitFilter key:" + key);
            Element element = cache.get(key);
            if (element == null) {
                element = new Element(key, new ConnectionBag());
                cache.put(element);
                filterChain.doFilter(req, resp);
            } else {
                ConnectionBag bag = (ConnectionBag) element.getObjectValue();
                if (bag.ok()) {
                    filterChain.doFilter(req, resp);
                } else {
                    HttpServletResponse httpR = (HttpServletResponse) resp;
                    httpR.setStatus(HttpURLConnection.HTTP_GONE);
                    httpR.getWriter().write("failed");
                }
            }
        } else {
            filterChain.doFilter(req, resp);
        }
    }

    private boolean exempt(String path) {
        // exempt some paths
    	return !(path.startsWith("/1/") || path.contains("/statuses/"));
    }

    public void destroy() {
        mgr.shutdown();
    }

    private class ConnectionBag {
        // all times are in milliseconds
        private long stamp = 0;
        private long delta = 2000;
        private int hitCount = 0;
        private final long start_delta = 2000;
        private final long max_delta = 1200000;
        private final long safePeriod = 300000;
        private final int threshold = 2;

        public ConnectionBag() {
        }

        public boolean ok() {
            boolean limitReached = false;
            long now = new Date().getTime();
            if (delta > max_delta) {
                delta = max_delta;
            }
            if (now <= stamp + delta) {
                ++hitCount;
                if (hitCount >= threshold) {
                    limitReached = true;
                    delta += delta;
                }
            } else {
                hitCount = 0;
            }
            if (now > stamp + safePeriod) {
                delta = start_delta;
                hitCount = 0;
            }
            stamp = now;
            return !limitReached;
        }
    }

}
