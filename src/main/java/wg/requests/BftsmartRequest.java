package wg.requests;

import java.util.ArrayList;

import wg.workload.ProtocolType;
import wg.workload.Request;

public class BftsmartRequest extends Request {

	private final String command;
	private final String type;
	private final ArrayList<String> targetGroup;

	public BftsmartRequest(String requestName, ProtocolType protocol, String command, String type,
			ArrayList<String> targetGroup) {
		super(requestName, protocol);
		this.command = command;
		this.type = type;
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
