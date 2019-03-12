package wg.responses;

import wg.workload.Target;

public abstract class Response {

	protected final Target[] targetGroup;
	private final long startTime;
	private final long stopTime;
	protected final boolean failed;

	public Response(long startTime, long stopTime, Target[] targets, boolean failed) {
		this.startTime = startTime;
		this.stopTime = stopTime;
		this.targetGroup = targets;
		this.failed = failed;
	}

	public Response(long startTime, long stopTime, Target target, boolean failed) {
		this.startTime = startTime;
		this.stopTime = stopTime;
		this.targetGroup = new Target[1];
		targetGroup[0] = target;
		this.failed = failed;
	}

	public long getRTT() {
		return stopTime - startTime;
	}

	public boolean isFailed() {
		return failed;
	}

	public abstract void print();

}
