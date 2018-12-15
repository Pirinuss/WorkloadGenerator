package wg.requests;

import wg.workload.Request;

public class HttpRequest extends Request {
	
	private HttpMethodType method;
	private String resourcePath;
	private String content;
	
	public HttpMethodType getMethod() {
		return method;
	}
	public void setMethod(HttpMethodType method) {
		this.method = method;
	}
	public String getResourcePath() {
		return resourcePath;
	}
	public void setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}

}
