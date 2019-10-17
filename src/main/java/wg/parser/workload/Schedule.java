package wg.parser.workload;

public class Schedule {

	private final Frame[] frames;

	public Schedule(Frame[] frames) {
		if (frames == null) {
			throw new IllegalArgumentException("Frames must not be null!");
		}
		this.frames = frames;
	}

	public Frame[] getFrames() {
		return frames;
	}

}
