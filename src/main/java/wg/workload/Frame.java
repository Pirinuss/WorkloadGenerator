package wg.workload;

public class Frame {

	private final String frameName;
	private final EventDescriptor[] events;
	private final Options options;

	public Frame(String frameName, EventDescriptor[] events, Options options) {
		this.frameName = frameName;
		this.events = events;
		this.options = options;
		if (events == null) {
			throw new IllegalArgumentException("No events found for " + frameName);
		}
		for (int i = 0; i < events.length; i++) {
			if (events[i] == null) {
				throw new IllegalArgumentException("Event " + i + 1 + " not found");
			}
		}
	}

	public String getFrameName() {
		return frameName;
	}

	public EventDescriptor[] getEvents() {
		return events;
	}

	public Options getOptions() {
		return options;
	}

}
