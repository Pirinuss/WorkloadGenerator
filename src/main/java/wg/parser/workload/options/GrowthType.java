package wg.parser.workload.options;

public enum GrowthType {

	LINEAR, INCREASEEXPO, INCREASEFIB;

	public static GrowthType parseString(String identifier) {
		switch (identifier) {
		case "linear":
			return LINEAR;
		case "exponentially":
			return INCREASEEXPO;
		case "fibonacci":
			return INCREASEFIB;
		default:
			throw new IllegalArgumentException(
					"Invalid identifier!" + identifier);
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
		}
		return null;
	}

}
