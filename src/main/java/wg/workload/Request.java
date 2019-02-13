package wg.workload;

public interface Request {

	public final String requestID;
	public final ProtocolType protocol;

	public Request(String requestID, ProtocolType protocol) {
		if (requestID == null) {
			throw new IllegalArgumentException("Request id must not be null!");
		}
		this.requestID = requestID;

		if (protocol == null) {
			throw new IllegalArgumentException("Protocol must not be null!");
		}
		this.protocol = protocol;
	}

	public String getRequestID() {
		return requestID;
	}

	public ProtocolType getProtocol() {
		return protocol;
	}

}
