package wg.workload;

public class Frame {
	
	String frameName;
	FrameModeType frameMode;
	long steps;
	EventDiscriptor[] events;
	
	public String getFrameName() {
		return frameName;
	}
	
	public void setFrameName(String frameName) {
		this.frameName = frameName;
	}
	
	public FrameModeType getFrameMode() {
		return frameMode;
	}

	public void setFrameMode(FrameModeType frameMode) {
		this.frameMode = frameMode;
	}

	public long getSteps() {
		return steps;
	}

	public void setSteps(long steps) {
		this.steps = steps;
	}

	public EventDiscriptor[] getEvents() {
		return events;
	}
	
	public void setEvents(EventDiscriptor[] events) {
		this.events = events;
	}

}
