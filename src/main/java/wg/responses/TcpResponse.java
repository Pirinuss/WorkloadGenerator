package wg.responses;

import wg.workload.Target;

public class TcpResponse extends Response {

	private final String content;

	public TcpResponse(long startTime, long endTime, Target target,
			String content, boolean failed) {
		super(startTime, endTime, target, failed);
		this.content = content;
	}

	@Override
	public void print() {
		// @formatter:off
		System.out.println("     Target: " + targetGroup[0].getServerName());
		System.out.println("     Execution time: " + getRTT());
		if (failed) {
			System.out.println("     Failed! Reason: " + content);
		} else {
			if (content.length() < 100) {
				System.out.println("     Response: " + content);
			} else {
				System.out.println("     Response length: " + content.length());
			}
		}
		// @formatter:on
	}
}