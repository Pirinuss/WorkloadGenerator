package wg.requests;

import java.net.Socket;

import wg.workload.ProtocolType;
import wg.workload.Request;

public class TcpRequest extends Request {

	private final String content;
	private Socket client;

	public TcpRequest(String requestName, ProtocolType protocol,
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

	public Socket getClient() {
		return client;
	}

	public void setClient(Socket client) {
		this.client = client;
	}

}
