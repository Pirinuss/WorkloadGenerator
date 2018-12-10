package wg.workload;

public class Schedule {

	Frame[] frames;

	public Frame[] getFrames() {
		return frames;
	}

	public void setFrames(Frame[] frames) {
		this.frames = frames;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("-----SCHEDULE-----");
		sb.append(System.getProperty("line.separator"));
		sb.append(System.getProperty("line.separator"));
		for (int i=0; i<frames.length; i++) {
			sb.append("---" + frames[i].getFrameName() + "---");
			sb.append(System.getProperty("line.separator"));
			EventDiscriptor[] events = frames[i].getEvents();
			for (int j=0; j<events.length; j++) {
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
