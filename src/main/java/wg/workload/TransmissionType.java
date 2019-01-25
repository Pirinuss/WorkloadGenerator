package wg.workload;

public enum TransmissionType {

	NONE, PARALLEL, SEQUENTIELL;
	
	public static TransmissionType parseString (String mode) {
		switch (mode) {
		case "parallel":
			return PARALLEL;
		case "sequentiell":
			return SEQUENTIELL;
		default:
			return NONE;	
		}
	}
}
