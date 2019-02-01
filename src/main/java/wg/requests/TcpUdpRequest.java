package wg.requests;

import wg.workload.ProtocolType;
import wg.workload.Request;

public class TcpUdpRequest extends Request {

	private final String content;

	public TcpUdpRequest(String requestName, ProtocolType protocol,
			String content) {

		super(requestName, protocol);

		if (content == null) {
			throw new IllegalArgumentException("Content must not be null");
		}
		this.content = content;
	}

	public String getContent() {
		return content;
	}

}
