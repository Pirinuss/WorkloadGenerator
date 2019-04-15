package wg.responses;

import java.util.Arrays;

import wg.parser.workload.Target;

public class BftsmartResponse extends Response {

	/** If the length of a reply is bigger than this value it won´t get print */
	private static final int MAX_LENGTH_FOR_PRINT = 100;
	private final byte[] reply;

	public BftsmartResponse(long startTime, long endTime, Target[] targetGroup,
			byte[] reply, boolean failed) {
		super(startTime, endTime, targetGroup, failed);
		this.reply = reply;
	}

	@Override
	public void print() {

		// @formatter:off
		System.out.println("     Number of targets: " + targetGroup.length);
		System.out.println("     Execution time: " + getRTT());
		if (reply == null) {
			System.out.println("     No reply received");
		} else {
			System.out.println("     Reply length: " + reply.length);
			if (reply.length < MAX_LENGTH_FOR_PRINT) {
				System.out.println("     Reply: " + Arrays.toString(reply));
			}
		}
		// @formatter:on
	}

}
