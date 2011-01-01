package neoauthlo.servlet;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthFilterServlet implements Filter {

	private static final Logger log = Logger.getLogger(AuthFilterServlet.class
			.getName());
	
	@SuppressWarnings("unused")
	private FilterConfig config;
	
	public AuthFilterServlet() {
	}

	public void init(FilterConfig filterConfig) throws ServletException {

		this.config = filterConfig;
		
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest)request;
		String sp = req.getRequestURI();
		log.info("getRequestURI: " + sp);
		String hostTarget = req.getServerName();
		log.info("getServerName: " + hostTarget);
		if (sp.startsWith("/1/") || sp.startsWith("/statuses/") || sp.startsWith("/system/")) {
				// Trying to make an API call to the WWW site
				if (!hostTarget.contains("localhost") && !hostTarget.startsWith("api.")) {
						String uri = "http://www.supertweet.net/about/api";
					log.info("Redirecting to " + uri);
					HttpServletResponse res = (HttpServletResponse) response;
					res.sendRedirect(uri);
					return;
				}
		} else if (hostTarget.startsWith("api.")) {
			// Trying to access a WWW page from the API site
			String uri = "http://" + hostTarget.replace("api", "www") + sp;
			log.info("Redirecting to " + uri);
			HttpServletResponse res = (HttpServletResponse) response;
			res.sendRedirect(uri);
			return;

		}
		
    	chain.doFilter(request, response);

	}// doFilter

	public void destroy() {
		/*
		 * called before the Filter instance is removed from service by the web
		 * container
		 */
	}

	
}
