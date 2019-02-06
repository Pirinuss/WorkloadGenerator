package wg.workload.options;

public class FrequencyOption {

	private final FrequencyMode mode;
	private final long factor;
	private final long steps;

	public FrequencyOption(FrequencyMode mode, long factor, long steps) {

		this.mode = mode;

		if (factor < 1) {
			throw new IllegalArgumentException("Invalid factor!");
		}
		this.factor = factor;

		if (steps < 1) {
			throw new IllegalArgumentException("Invalid step value!");
		}
		this.steps = steps;
	}

	public FrequencyMode getMode() {
		return mode;
	}

	public long getFactor() {
		return factor;
	}

	public long getSteps() {
		return steps;
	}

}
