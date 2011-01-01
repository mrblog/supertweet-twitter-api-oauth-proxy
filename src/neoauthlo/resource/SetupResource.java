package neoauthlo.resource;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

public class SetupResource extends BaseResource {

	private String title;
	private StringBuffer page;
	
	private final static String styles = "*{margin:0; padding:0;}\n\nbody {\nfont-family: Verdana, Arial, \"Trebuchet MS\", Sans-Serif, Georgia, Courier, \"Times New Roman\", Serif;\nfont-size: 12px; \nbackground: #346083;\ncolor: #333; \nmargin:0; \npadding:0;\nline-height: 1.4;\n}\n\na { color: #27699F; text-decoration: none; }\na:hover { text-decoration: underline; }\n\nimg{ border: none; padding: 6px; } \nimg a{border:none;} \n\nul { list-style-type: none; }\n\nh1              { font-size: 2em; margin: .67em 0 }\nh3              { font-size: 1.17em; margin: .83em 0 }\nh4, p,\nblockquote, ul,\nfieldset, form,\nol, dl, dir,\nmenu            { margin: 1.12em 0 }\n\nh2 {\n    background: #374B5C;\n    height: 30px;\n    line-height: 30px;\n    font-weight: 600;\n    font-size: 12px;\n    margin: 20px 0 20px 0; padding: 0 0 0 10px; \n    color: #fff;\n}\n\n#page {\nwidth: 800px;\nmargin: 20px auto 10px auto;\n}\n\n#header h1 {\nfont-size: 19px;\nfont-weight: 600;\npadding: 0 0 0 20px;\nletter-spacing: -1px;\n}\n#header h1 a {\ncolor: #fff;\n}\n#header h1 a:hover {\nborder-bottom: 1px solid #fff; text-decoration: none;\n}\n#header h2 {\ncolor: #fff;\nfont-size: 15px;\nfont-weight: 100;\npadding: 0 0 0 20px;\n}\n\n#wrapper {\npadding: 10px;\nmargin: 0 10px;\nbackground: #fff;\n}\n\n#content {\npadding-bottom: 20px;\n}\n\n#footer {\nheight: 40px;\nline-height: 40px;\nfont-weight: 100;\nfont-size: 12px;\ntext-align: center;\ncolor #fff;	\n}\n#footer p { color: #fff; }\n#footer a { color: #fff; text-decoration: none; }\n#footer a:hover { text-decoration: underline; }\n";

	protected void setTitle(String title) {
		this.title = title;
	}
	protected String  errorHTML(String errmsg) {
		return htmlWrapper("<h2>Error</h2><p style=\"text-align: center; padding: 5px; color:#545454; width:80%;  margin:5px auto;; background-color: #ffcdd1; border-top: 2px solid #e10c0c; border-bottom: 2px solid #e10c0c; \">" + errmsg + "</p>");
	}
	protected Representation errorRepresentation(Status status, String errmsg) {
		if (status.getCode() != 200) 
			getResponse().setStatus(status);
		//String req = getRequest().getResourceRef().getPath();
		return new StringRepresentation(errorHTML(errmsg), MediaType.TEXT_HTML);
	}
	
	protected void htmlOpen() {
		page = new StringBuffer();
		page.append("<html><head><title>");
		page.append(title);
		page.append("</title><style>");
		page.append(styles);
		page.append("</style></head><body><div id=\"page\"><div id=\"header\"> \n<h1><a href=\"#\">SuperTweet Proxy</a></h1> \n<h2>Basic Auth to OAuth proxy for the Twitter API</h2> \n</div>\n<div id=\"wrapper\"><div id=\"content\">");
	}
	
	protected void htmlAppend(String content) {
		page.append(content);
	}
	
	protected void htmlClose() {
		page.append("</div></div><div id=\"footer\"> \n<p><a href=\"http://www.supertweet.net/about/localproxy\">Help</a> | Brought to you by <a href=\"http://www.supertweet.net\">SuperTweet.net</a> \n</p> \n</div></div></body></html>");
	}	
	
	protected String htmlWrapper(String content) {
		htmlOpen();
		htmlAppend(content);
		htmlClose();
		return page.toString();
	}
	protected String htmlContent() {
		return page.toString();
	}
	
}
