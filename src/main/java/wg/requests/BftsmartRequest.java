package wg.requests;

import java.util.ArrayList;

import wg.workload.ProtocolType;
import wg.workload.Request;

public class BftsmartRequest extends Request {

	private final String command;
	private final String type;
	private final ArrayList<String> targetGroup;

	public BftsmartRequest(String requestName, ProtocolType protocol,
			String command, String type, ArrayList<String> targetGroup) {

		super(requestName, protocol);

		if (command == null) {
			throw new IllegalArgumentException("Command must not be null!");
		}
		this.command = command;

		if (type == null) {
			throw new IllegalArgumentException("Type must not be null!");
		}
		this.type = type;

		if (targetGroup == null) {
			throw new IllegalArgumentException(
					"Target group must not be null!");
		}
		this.targetGroup = targetGroup;
	}

	public String getCommand() {
		return command;
	}

	public String getType() {
		return type;
	}

	public ArrayList<String> getTargetGroup() {
		return targetGroup;
	}

	public void addTarget(String target) {
		targetGroup.add(target);
	}

}
