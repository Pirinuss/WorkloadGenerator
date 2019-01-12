package wg.workload;

public class EventDiscriptor {
	
	String eventName;
	long time;
	long repetitions;
	String targetName;
	String requestName;
	
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public long getRepetitions() {
		return repetitions;
	}
	public void setRepetitions(long repetitions) {
		this.repetitions = repetitions;
	}
	public String getTargetName() {
		return targetName;
	}
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}
	public String getRequestName() {
		return requestName;
	}
	public void setRequestName(String requestName) {
		this.requestName = requestName;
	}

}
