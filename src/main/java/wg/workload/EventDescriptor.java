package wg.workload;

import wg.requests.Request;

public class EventDescriptor implements Comparable<EventDescriptor> {

	private final String eventID;
	private final Target targets[];
	private Request request;
	private final long time;

	public EventDescriptor(String eventID, long time, Target[] targets,
			Request request) {
		if (eventID == null) {
			throw new IllegalArgumentException("Event id must not be null!");
		}
		this.eventID = eventID;

		if (targets == null || targets.length < 1) {
			throw new IllegalArgumentException("Target must not be null!");
		}
		this.targets = targets;

		if (request == null) {
			throw new IllegalArgumentException("Request must not be null!");
		}
		this.request = request;

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

	public Target[] getTargets() {
		return targets;
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	@Override
	public int compareTo(EventDescriptor descriptor) {
		return (Integer) Long.compare(time, descriptor.getTime());
	}

}
