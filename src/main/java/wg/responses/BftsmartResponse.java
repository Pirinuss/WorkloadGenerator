package wg.responses;

import wg.core.Response;
import wg.workload.Target;

public class BftsmartResponse extends Response {

	private final byte[] reply;

	public BftsmartResponse(long startTime, long endTime, Target[] targetGroup,
			byte[] reply) {
		super(startTime, endTime, targetGroup);
		this.reply = reply;
	}

	public byte[] getReply() {
		return reply;
	}

}
