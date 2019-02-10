package wg.requests;

public class BftsmartCommand {

	private final BftsmartCommandType type;
	private final String content;
	private final Object[] objects;

	public BftsmartCommand(BftsmartCommandType type, String content,
			Object[] objects) {

		if (type == null) {
			throw new IllegalArgumentException("Type must not be null!");
		}
		this.type = type;

		if (content == null) {
			throw new IllegalArgumentException("Content must not be null!");
		}
		this.content = content;

		this.objects = objects;
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
