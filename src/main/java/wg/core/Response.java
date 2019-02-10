package wg.core;

import wg.workload.Target;

public class Response {

	private final Target target;
	private final Target[] targetGroup;
	private final long startTime;
	private final long stopTime;

	public Response(long startTime, long stopTime, Target target) {
		this.startTime = startTime;
		this.stopTime = stopTime;
		this.target = target;
		this.targetGroup = null;
	}
	
	public Response(long startTime, long stopTime, Target[] targets) {
		this.startTime = startTime;
		this.stopTime = stopTime;
		this.targetGroup = targets;
		this.target = null;
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
	
	public Target[] getTargetGroup() {
		return targetGroup;
	}

}
