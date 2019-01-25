package wg.workload;

public class Frame {
	
	private final String frameName;
	private final EventDiscriptor[] events;
	private final Options options;
	
	public Frame(String frameName, EventDiscriptor[] events, Options options) {
		super();
		this.frameName = frameName;
		this.events = events;
		this.options = options;
	}

	public String getFrameName() {
		return frameName;
	}

	public EventDiscriptor[] getEvents() {
		return events;
	}

	public Options getOptions() {
		return options;
	}
	

}
