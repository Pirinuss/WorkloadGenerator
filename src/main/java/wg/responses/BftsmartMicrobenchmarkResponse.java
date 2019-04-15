package wg.responses;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import wg.parser.workload.Target;

public class BftsmartMicrobenchmarkResponse extends Response {

	/** If the length of a reply is bigger than this value it won´t get print */
	//private static final int MAX_LENGTH_FOR_PRINT = 100;
	private final byte[] reply;

	public BftsmartMicrobenchmarkResponse(long startTime, long endTime,
			Target[] targetGroup, byte[] reply, boolean failed) {
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
				ByteArrayInputStream in = new ByteArrayInputStream(reply);
			    ObjectInputStream is;
				try {
					is = new ObjectInputStream(in);
					float tp = is.readFloat();
				    double latency = is.readDouble();
				    System.out.println("     Throughput: " + tp);
				    System.out.println("     Latency: " + latency);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		// @formatter:on		
	}

}
