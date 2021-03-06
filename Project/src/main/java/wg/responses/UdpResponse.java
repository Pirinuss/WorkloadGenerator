package wg.responses;

import wg.parser.workload.Target;

public class UdpResponse extends Response {

	private final String content;

	public UdpResponse(long startTime, long endTime, Target target,
			String content, boolean failed) {
		super(startTime, endTime, target, failed);
		this.content = content;
	}

	public String getContent() {
		return content;
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