package wg.responses;

import wg.core.Response;
import wg.workload.Target;

public class BftsmartResponse extends Response {

	private final byte[] reply;

	public BftsmartResponse(long startTime, long endTime, Target target,
			byte[] reply) {
		super(startTime, endTime, target);
		this.reply = reply;
	}

	public byte[] getReply() {
		return reply;
	}

}
