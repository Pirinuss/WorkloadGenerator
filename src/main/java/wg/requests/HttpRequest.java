package wg.requests;

import wg.workload.ProtocolType;
import wg.workload.Request;

public class HttpRequest extends Request {

	private final HttpMethodType method;
	private final String resourcePath;
	private final String content;

	public HttpRequest(String requestName, ProtocolType protocol,
			HttpMethodType method, String resourcePath, String content) {

		super(requestName, protocol);

		if (method == null) {
			throw new IllegalArgumentException("Method must not be null!");
		}
		this.method = method;

		if (resourcePath == null) {
			resourcePath = "";
		}
		this.resourcePath = resourcePath;

		if (content == null) {
			content = "";
		}
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
