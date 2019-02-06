package wg.workload;

public class EventDescriptor implements Comparable<EventDescriptor> {

	private final String eventID;
	private final Target target;
	private Request request;
	private final long time;

	public EventDescriptor(String eventID, long time, Target target,
			Request request) {
		if (eventID == null) {
			throw new IllegalArgumentException("Event id must not be null!");
		}
		this.eventID = eventID;

		if (target == null) {
			if (request.getProtocol() != ProtocolType.BFTSMaRt) {
				throw new IllegalArgumentException("Target must not be null!");
			}
		}
		this.target = target;

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

	public Target getTarget() {
		return target;
	}

	public Request getRequest() {
		return request;
	}
	
	public void setRequest(Request request) {
		this.request = request;
	}

	@Override
	public int compareTo(EventDescriptor o) {
		return (Integer) Long.compare(time, o.getTime());
	}

}
