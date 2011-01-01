package neoauthlo.resource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import neoauthlo.NeoAuthApplication;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

public class GeneralAPIProxyResource extends ProxyResource {
	
    private final static Logger logger = Logger.getLogger(GeneralAPIProxyResource.class.getName());

    @Get
    public Representation represent() {
    	Map<String, String> fields = new HashMap<String, String>();
        Form form = getRequest().getResourceRef().getQueryAsForm();
        if (form != null) {
        	fields = form.getValuesMap();
        }
        //logger.info("Path: " + getRequest().getResourceRef().getPath() + " fields: " + fields);
        return doGetRequest(fields);
    }

    @SuppressWarnings("unused")
	private void logPOST(String data) {
		String req = getRequest().getResourceRef().getPath();
		File f = new File(NeoAuthApplication.logPath + "/POSTlog");
		try {
			FileWriter fw = new FileWriter(f, true);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			fw.write(sdf.format(new Date()));
			fw.write(" ");
			fw.write(req);
			fw.write(" ");
			fw.write(data);
			fw.write('\n');
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @Post
	public Representation acceptRepresentation(Representation entity)
			throws ResourceException {
    	Map<String, String> fields = new HashMap<String, String>();

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

    @Delete
    public Representation deleteRepresent() {
    	Map<String, String> fields = new HashMap<String, String>();
        Form form = getRequest().getResourceRef().getQueryAsForm();
        if (form != null) {
        	fields = form.getValuesMap();
        }
        logger.info("DELETE Path: " + getRequest().getResourceRef().getPath() + " fields: " + fields);
        return doDeleteRequest(fields);
    }
}
