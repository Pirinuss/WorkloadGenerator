package wg.requests;

import wg.workload.Request;

public class HttpRequest extends Request {
	
	private HttpMethodType method;
	private String resourcePath;
	private byte[] content;
	
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
	public byte[] getContent() {
		return content;
	}
	public void setContent(byte[] content) {
		this.content = content;
	}

}
