package wg.responses;

import wg.workload.Target;

public class Response implements ResponseInterface {

	private final Target[] targetGroup;
	private final long startTime;
	private final long stopTime;

	public Response(long startTime, long stopTime, Target[] targets) {
		this.startTime = startTime;
		this.stopTime = stopTime;
		this.targetGroup = targets;
	}

	public Response(long startTime, long stopTime, Target target) {
		this.startTime = startTime;
		this.stopTime = stopTime;
		this.targetGroup = new Target[1];
		targetGroup[0] = target;
	}

	public long getRTT() {
		return stopTime - startTime;
	}

	public Target[] getTargetGroup() {
		return targetGroup;
	}

	@Override
	public void print() {

	}

}
