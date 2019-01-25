package wg.workload;

public class Request {

	private final String requestName;
	private final ProtocolType protocol;

	public Request(String requestName, ProtocolType protocol) {
		this.requestName = requestName;
		this.protocol = protocol;
	}

	public String getRequestName() {
		return requestName;
	}

	public ProtocolType getProtocol() {
		return protocol;
	}
	
}
