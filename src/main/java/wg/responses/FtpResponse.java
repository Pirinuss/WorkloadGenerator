package wg.responses;

import wg.core.Response;
import wg.workload.Target;

public class FtpResponse extends Response {

	private final int replyCode;

	public FtpResponse(long startTime, long endTime, Target target,
			int replyCode) {
		super(startTime, endTime, target);
		this.replyCode = replyCode;
	}

	public int getReplyCode() {
		return replyCode;
	}

}
