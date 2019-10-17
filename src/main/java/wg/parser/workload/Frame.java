package wg.parser.workload;

import wg.parser.workload.options.Options;

public class Frame {

	private final String frameID;
	private final EventDescriptor[] events;
	private final Options options;

	public Frame(String frameID, EventDescriptor[] events, Options options) {
		if (frameID == null) {
			throw new IllegalArgumentException("Frame id must not be null!");
		}
		this.frameID = frameID;
		
		if (events == null) {
			throw new IllegalArgumentException("Events must not be null!");
		}
		this.events = events;
		
		this.options = options;
	}

	public String getFrameID() {
		return frameID;
	}

	public EventDescriptor[] getEvents() {
		return events;
	}

	public Options getOptions() {
		return options;
	}

}
