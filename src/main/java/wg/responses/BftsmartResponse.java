package wg.responses;

import wg.workload.Target;

public class BftsmartResponse extends Response implements ResponseInterface {

	private final byte[] reply;

	public BftsmartResponse(long startTime, long endTime, Target[] targetGroup,
			byte[] reply) {
		super(startTime, endTime, targetGroup);
		this.reply = reply;
	}

	@Override
	public void print() {
		// @formatter:off
		System.out.println("     Number of targets: " + getTargetGroup().length);
		System.out.println("     Execution time: " + getRTT());
		System.out.println("     Reply length: " + reply.length);
		// @formatter:on
	}

}
