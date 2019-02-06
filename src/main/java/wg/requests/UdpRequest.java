package wg.requests;

import java.net.DatagramSocket;
import wg.workload.ProtocolType;
import wg.workload.Request;

public class UdpRequest extends Request {

	private final String content;
	private DatagramSocket client;

	public UdpRequest(String requestName, ProtocolType protocol,
			String content) {

		super(requestName, protocol);

		if (content == null) {
			throw new IllegalArgumentException("Content must not be null!");
		}
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	public DatagramSocket getClient() {
		return client;
	}

	public void setClient(DatagramSocket client) {
		this.client = client;
	}

}
