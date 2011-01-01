// created Sep 24, 2009 6:23:43 PM
// by petrovic

package neoauthlo.resource;

import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.restlet.resource.ServerResource;

public class BaseResource extends ServerResource {
    @SuppressWarnings("unused")
    private static Logger LOG = Logger.getLogger(BaseResource.class.getName());

    protected Object getContextAttribute(String key) {
        Map<String, Object> attrs = getContext().getAttributes();
        return attrs.get(key);
    }
    
    protected Object getServletContextAttribute(String key) {
        
        Map<String, Object> appattrs = getApplication().getContext().getAttributes();;
        ServletContext sc = (ServletContext) appattrs.get("org.restlet.ext.servlet.ServletContext");
        if (sc != null) {
        	return sc.getAttribute(key);
        }
        return null;
    }
    
}
