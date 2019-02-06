package wg.workload.options;

public enum TransmissionType {

	PARALLEL, SEQUENTIELL;

	public static TransmissionType parseString(String identifier) {
		switch (identifier) {
		case "parallel":
			return PARALLEL;
		case "sequentiell":
			return SEQUENTIELL;
		default:
			throw new IllegalArgumentException(
					"Unknown identifier! " + identifier);
		}
	}
}
