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

}
