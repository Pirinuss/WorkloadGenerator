package wg.requests;

import org.json.simple.JSONObject;

public class BftsmartCommand {

	private final BftsmartCommandType type;
	private final String content;
	private final Object[] objects;

	public BftsmartCommand(JSONObject command) {

		String type = (String) command.get("type");
		if (type == null) {
			throw new IllegalArgumentException(
					"Command type must not be null!");
		}
		BftsmartCommandType commandType = BftsmartCommandType.fromString(type);
		this.type = commandType;

		String content = (String) command.get("content");
		if (content == null) {
			throw new IllegalArgumentException("Content must not be null!");
		}
		this.content = content;

		Object[] objects = null;
		if (commandType == BftsmartCommandType.BYTE_OBJECT_STREAM) {
			String[] params = content.split(",");
			if (params.length == 0) {
				throw new IllegalArgumentException("Invalid content!");
			}
			objects = new Object[params.length];
			for (int i = 0; i < params.length; i++) {
				String[] param = params[i].split(":");
				if (param.length != 2) {
					throw new IllegalArgumentException("Invalid content!");
				}
				String id = param[0];
				String identifier = param[1];
				if (command.get(id) == null) {
					throw new IllegalArgumentException(
							"Content parameter not found! " + id);
				}
				try {
					objects[i] = getObject(identifier,
							command.get(id).toString());
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(
							"Invalid parameter content! " + id);
				}
			}
		}
		this.objects = objects;

	}

	private static Object getObject(String identifier, String stringParam) {
		Object object;
		switch (identifier.toUpperCase()) {
		case "BOOLEAN":
			if (!stringParam.toUpperCase().equals("TRUE")
					&& !stringParam.toUpperCase().equals("FALSE")) {
				throw new IllegalArgumentException("Invalid parameter!");
			}
			object = Boolean.valueOf(stringParam);
			break;
		case "CHAR":
			if (stringParam.length() != 1) {
				throw new IllegalArgumentException("Invalid parameter!");
			}
			object = Character.valueOf(stringParam.charAt(0));
			break;
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
			object = String.valueOf(stringParam);
			break;
		default:
			throw new IllegalArgumentException(
					"Unknown identifier" + identifier);
		}
		return object;
	}

	public BftsmartCommandType getType() {
		return type;
	}

	public String getContent() {
		return content;
	}

	public Object[] getObjects() {
		return objects;
	}

}
