package wg;

public enum DataType {

	// @formatter:off
	CHAR("char"),
	BYTE("BYTE"),
	INT("int"),
	LONG("long"),
	STRING("string");
	// @formatter:on

	private DataType(String identifier) {
		this.identfier = identifier;
	}

	private final String identfier;

	public String getIdentfier() {
		return identfier;
	}

	public static DataType fromString(String identifier) {
		switch (identifier) {
		case "char":
			return CHAR;
		default:
			throw new IllegalArgumentException(
					"Unknown identifier! " + identifier);
		}
	}
}
