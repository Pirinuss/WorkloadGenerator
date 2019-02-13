package wg.workload;

public class Request {

	private final String requestID;
	private final ProtocolType protocol;
	private long numberOfClients;

	public Request(String requestID, ProtocolType protocol,
			long numberOfClients) {
		if (requestID == null) {
			throw new IllegalArgumentException("Request id must not be null!");
		}
		this.requestID = requestID;

		if (protocol == null) {
			throw new IllegalArgumentException("Protocol must not be null!");
		}
		this.protocol = protocol;
		
		if (numberOfClients < 1) {
			throw new IllegalArgumentException("At least one client required!");
		}
		this.numberOfClients = numberOfClients;
	}

	public String getRequestID() {
		return requestID;
	}

	public ProtocolType getProtocol() {
		return protocol;
	}
	
	public long getNumberOfClients() {
		return numberOfClients;
	}

}
