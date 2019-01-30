package wg.workload;

public class EventDescriptor implements Comparable<EventDescriptor> {

	private final String eventName;
	private final String targetName;
	private final String requestName;
	private final long time;

	public EventDescriptor(String eventName, long time, String targetName,
			String requestName) {
		this.eventName = eventName;
		this.time = time;
		this.targetName = targetName;
		this.requestName = requestName;
	}

	public String getEventName() {
		return eventName;
	}

	public long getTime() {
		return time;
	}

	public String getTargetName() {
		return targetName;
	}

	public String getRequestName() {
		return requestName;
	}

	@Override
	public int compareTo(EventDescriptor o) {
		// TODO Auto-generated method stub
		return 0;
	}

}
