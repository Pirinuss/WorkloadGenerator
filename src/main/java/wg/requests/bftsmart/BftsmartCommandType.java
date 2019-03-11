package wg.requests.bftsmart;

public enum BftsmartCommandType {

	DATA_OUTPUT_STREAM, OBJECT_OUTPUT_STREAM;

	public static BftsmartCommandType fromString(String identifier) {

		switch (identifier.toUpperCase()) {
		case "DATAOUTPUTSTREAM":
			return DATA_OUTPUT_STREAM;
		case "OBJECTOUTPUTSTREAM":
			return OBJECT_OUTPUT_STREAM;
		default:
			throw new IllegalArgumentException(
					"Unknown identifier! " + identifier);
		}

	}

}
