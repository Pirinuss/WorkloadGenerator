package wg.responses;

import wg.workload.Target;

public class FtpResponse extends Response {

	private final int replyCode;

	public FtpResponse(long startTime, long endTime, Target target,
			int replyCode) {
		super(startTime, endTime, target);
		this.replyCode = replyCode;
	}

	@Override
	public void print() {
		// @formatter:off
		System.out.println("     Target: " + targetGroup[0].getServerName());
		System.out.println("     Execution time: " + getRTT());
		System.out.println("     Reply code: " + replyCode);
		// @formatter:on
	}

}
