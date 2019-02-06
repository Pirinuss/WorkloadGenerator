package wg.requests;

import bftsmart.tom.ServiceProxy;
import wg.workload.ProtocolType;
import wg.workload.Request;
import wg.workload.Target;

public class BftsmartRequest extends Request {

	private final String command;
	private final String type;
	private final Target[] targetGroup;
	private ServiceProxy client;

	public BftsmartRequest(String requestName, ProtocolType protocol,
			String command, String type, Target[] targetGroup) {

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

	public Target[] getTargetGroup() {
		return targetGroup;
	}

	public ServiceProxy getClient() {
		return client;
	}

	public void setClient(ServiceProxy client) {
		this.client = client;
	}

}
