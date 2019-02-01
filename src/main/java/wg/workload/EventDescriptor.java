package wg.workload;

public class EventDescriptor implements Comparable<EventDescriptor> {

	private final String eventID;
	private final String targetName;
	private final String requestName;
	private final long time;

	public EventDescriptor(String eventID, long time, String targetName,
			String requestName) {
		if (eventID == null) {
			throw new IllegalArgumentException("Event id must not be null!");
		}
		this.eventID = eventID;

		if (targetName == null) {
			throw new IllegalArgumentException("Target name must not be null!");
		}
		this.targetName = targetName;

		if (requestName == null) {
			throw new IllegalArgumentException(
					"Request name must not be null!");
		}
		this.requestName = requestName;

		if (time < 0) {
			throw new IllegalArgumentException("Invalid time value!");
		}
		this.time = time;
	}

	public String getEventID() {
		return eventID;
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
