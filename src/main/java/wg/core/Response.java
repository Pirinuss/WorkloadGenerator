package wg.core;

import wg.workload.Target;

public class Response {

	private final Target target;
	private final long startTime;
	private final long stopTime;

	public Response(long startTime, long stopTime, Target target) {
		this.startTime = startTime;
		this.stopTime = stopTime;
		this.target = target;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getStopTime() {
		return stopTime;
	}
	
	public Target getTarget() {
		return target;
	}

}
