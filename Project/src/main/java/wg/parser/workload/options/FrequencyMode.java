package wg.parser.workload.options;

public enum FrequencyMode {

	INCREASE, DECREASE;

	public static FrequencyMode parseString(String identifier) {
		switch (identifier) {
		case "increase":
			return INCREASE;
		case "decrease":
			return DECREASE;
		default:
			throw new IllegalArgumentException(
					"Invalid identifier! " + identifier);
		}
	}

}
