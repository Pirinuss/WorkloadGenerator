package wg.requests;

public enum BftsmartCommandType {

	BYTE_ARRAY, BYTE_OBJECT_STREAM;

	public static BftsmartCommandType fromString(String identifier) {

		switch (identifier.toUpperCase()) {
		case "BYTEARRAY":
			return BYTE_ARRAY;
		case "BYTEOBJECTSTREAM":
			return BYTE_OBJECT_STREAM;
		default:
			throw new IllegalArgumentException(
					"Unknown identifier! " + identifier);
		}

	}

}
