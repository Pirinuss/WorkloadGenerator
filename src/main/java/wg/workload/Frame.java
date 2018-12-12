package wg.workload;

public class Frame {
	
	String frameName;
	EventDiscriptor[] events;
	
	public String getFrameName() {
		return frameName;
	}
	public void setFrameName(String frameName) {
		this.frameName = frameName;
	}
	public EventDiscriptor[] getEvents() {
		return events;
	}
	public void setEvents(EventDiscriptor[] events) {
		this.events = events;
	}
	
	public EventDiscriptor getEventDisriptorByName(String eventName) {
		for (int i=0; i<events.length; i++) {
			if (events[i].getEventName().equals(eventName)) {
				return events[i];
			}
		} return null;
	}

}
