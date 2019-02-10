package wg;

public enum DataType {

	// @formatter:off
	BOOLEAN("boolean"),
	CHAR("char"),
	BYTE("BYTE"),
	SHORT("short"),
	INT("int"),
	LONG("long"),
	FLOAT("float"),
	DOUBLE("double"),
	STRING("string");
	// @formatter:on

	private final String identfier;

	private DataType(String identifier) {
		this.identfier = identifier;
	}

	public String getIdentfier() {
		return identfier;
	}

	public static DataType fromString(String identifier) {
		switch (identifier.toUpperCase()) {
		case "BOOLEAN":
			return BOOLEAN;
		case "CHAR":
			return CHAR;
		case "BYTE":
			return BYTE;
		case "SHORT":
			return SHORT;
		case "INT":
			return INT;
		case "LONG":
			return LONG;
		case "FLOAT":
			return FLOAT;
		case "DOUBLE":
			return DOUBLE;
		case "STRING":
			return STRING;
		default:
			throw new IllegalArgumentException(
					"Unknown identifier! " + identifier);
		}
	}

	public static Object getObject(String identifier, String stringParam) {
		Object object;
		switch (identifier.toUpperCase()) {
		case "BOOLEAN":
			if (!stringParam.toUpperCase().equals("TRUE")
					|| !stringParam.toUpperCase().equals("FALSE")) {
				throw new IllegalArgumentException("Invalid parameter!");
			}
			object = Boolean.valueOf(stringParam);
		case "CHAR":
			if (stringParam.length() != 1) {
				throw new IllegalArgumentException("Invalid parameter!");
			}
			object = Character.valueOf(stringParam.charAt(0));
		case "BYTE":
			object = Byte.valueOf(stringParam);
			break;
		case "SHORT":
			object = Short.valueOf(stringParam);
			break;
		case "INT":
			object = Integer.valueOf(stringParam);
			break;
		case "LONG":
			object = Long.valueOf(stringParam);
			break;
		case "FLOAT":
			object = Float.valueOf(stringParam);
			break;
		case "DOUBLE":
			object = Double.valueOf(stringParam);
			break;
		case "STRING":
			object = new String(stringParam);
		default:
			throw new IllegalArgumentException(
					"Unknown identifier" + identifier);
		}
		return object;
	}
}
