package wg.responses;

import wg.workload.Target;

public class UdpResponse extends Response {

	private final String content;

	public UdpResponse(long startTime, long endTime, Target target,
			String content) {
		super(startTime, endTime, target);
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
		if (content.length() < 100) {
			System.out.println("     Response: " + content);
		} else {
			System.out.println("     Response length: " + content.length());
		}
		// @formatter:on
	}
}