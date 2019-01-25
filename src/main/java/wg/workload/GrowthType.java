package wg.workload;

public enum GrowthType {

	NONE, LINEAR, INCREASEEXPO, INCREASEFIB;

	public static GrowthType parseString(String mode) {
		switch (mode) {
		case "linear":
			return LINEAR;
		case "increaseExpo":
			return INCREASEEXPO;
		case "increaseFib":
			return INCREASEFIB;
		default:
			return NONE;
		}
	}

}
