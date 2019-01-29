package wg.workload;

public enum GrowthType {

	NONE, LINEAR, INCREASEEXPO, INCREASEFIB;

	public static GrowthType parseString(String mode) {
		switch (mode) {
		case "linear":
			return LINEAR;
		case "exponentially":
			return INCREASEEXPO;
		case "fibonacci":
			return INCREASEFIB;
		default:
			return NONE;
		}
	}
	
	public static String parseGrowthType(GrowthType type) {
		switch (type) {
		case INCREASEEXPO:
			return "exponentially";
		case INCREASEFIB:
			return "fibonacci";
		case LINEAR:
			return "linear";
		case NONE:
			return null;
		}
		return null;
	}

}
