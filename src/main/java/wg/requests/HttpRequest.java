package wg.requests;

import wg.workload.ProtocolType;
import wg.workload.Request;

public class HttpRequest extends Request {

	private final HttpMethodType method;
	private final String resourcePath;
	private final String content;

	public HttpRequest(String requestName, ProtocolType protocol, HttpMethodType method, String resourcePath,
			String content) {
		super(requestName, protocol);
		this.method = method;
		this.resourcePath = resourcePath;
		this.content = content;
	}

	public HttpMethodType getMethod() {
		return method;
	}

	public String getResourcePath() {
		return resourcePath;
	}

	public String getContent() {
		return content;
	}

}
