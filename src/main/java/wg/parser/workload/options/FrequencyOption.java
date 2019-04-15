package wg.parser.workload.options;

public class FrequencyOption {

	private final FrequencyMode mode;
	private final long factor;

	public FrequencyOption(FrequencyMode mode, long factor) {

		this.mode = mode;

		if (factor < 1) {
			throw new IllegalArgumentException("Invalid factor!");
		}
		this.factor = factor;
	}

	public FrequencyMode getMode() {
		return mode;
	}

	public long getFactor() {
		return factor;
	}

}
