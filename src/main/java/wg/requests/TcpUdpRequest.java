package wg.requests;

import wg.workload.Request;

public class TcpUdpRequest extends Request {

	String content;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
}
