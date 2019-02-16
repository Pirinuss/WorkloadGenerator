package wg.workload.options;

public enum TransmissionType {

	PARALLEL, SEQUENTIAL;

	public static TransmissionType parseString(String identifier) {
		switch (identifier.toUpperCase()) {
		case "PARALLEL":
			return PARALLEL;
		case "SEQUENTIAL":
			return SEQUENTIAL;
		default:
			throw new IllegalArgumentException(
					"Unknown identifier! " + identifier);
		}
	}
}
