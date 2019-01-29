package wg.workload;

public class Schedule {

	private final Frame[] frames;

	public Schedule(Frame[] frames) {
		this.frames = frames;
		if (frames == null) {
			throw new IllegalArgumentException("No frames found");
		}
		for (int i = 0; i < frames.length; i++) {
			if (frames[i] == null) {
				throw new IllegalArgumentException("Frame " + i + 1 + " not found");
			}
		}
	}

	public Frame[] getFrames() {
		return frames;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("-----SCHEDULE-----");
		sb.append(System.getProperty("line.separator"));
		sb.append(System.getProperty("line.separator"));
		for (int i = 0; i < frames.length; i++) {
			sb.append("---" + frames[i].getFrameName() + "---");
			sb.append(System.getProperty("line.separator"));
			EventDiscriptor[] events = frames[i].getEvents();
			for (int j = 0; j < events.length; j++) {
				sb.append("   " + events[j].getEventName());
				sb.append(System.getProperty("line.separator"));
				sb.append("   " + events[j].getRequestName());
				sb.append(System.getProperty("line.separator"));
				sb.append("   " + events[j].getTargetName());
				sb.append(System.getProperty("line.separator"));
				sb.append("   " + events[j].getTime());
				sb.append(System.getProperty("line.separator"));
				sb.append(System.getProperty("line.separator"));
			}
		}
		return sb.toString();
	}

}
