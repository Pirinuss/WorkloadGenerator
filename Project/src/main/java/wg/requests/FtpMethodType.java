package wg.requests;

public enum FtpMethodType {

	GET, PUT;

	public static FtpMethodType fromString(String identifier) {
		switch (identifier.toUpperCase()) {
		case "GET":
			return GET;
		case "PUT":
			return PUT;
		default:
			throw new IllegalArgumentException(
					"Unknown identifier! " + identifier);
		}
	}

}
