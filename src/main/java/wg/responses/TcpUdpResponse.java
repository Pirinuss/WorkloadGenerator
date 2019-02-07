package wg.responses;

import wg.core.Response;
import wg.workload.Target;

public class TcpUdpResponse extends Response {

	private final String content;

	public TcpUdpResponse(long startTime, long endTime, Target target,
			String content) {
		super(startTime, endTime, target);
		this.content = content;
	}

	public String getContent() {
		return content;
	}

}
