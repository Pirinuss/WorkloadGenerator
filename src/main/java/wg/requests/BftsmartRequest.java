package wg.requests;

import bftsmart.tom.ServiceProxy;
import wg.workload.ProtocolType;
import wg.workload.Request;
import wg.workload.Target;

public class BftsmartRequest extends Request {

	private final BftsmartCommand command;
	private final String type;
	private final Target[] targetGroup;
	private ServiceProxy client;

	public BftsmartRequest(String requestName, ProtocolType protocol,
			BftsmartCommand command, String type, Target[] targetGroup) {

		super(requestName, protocol);

		if (command == null) {
			throw new IllegalArgumentException("Command must not be null!");
		}
		this.command = command;

		if (type == null) {
			throw new IllegalArgumentException("Type must not be null!");
		}
		if (!type.toUpperCase().equals("ORDERED")
				&& !type.toUpperCase().equals("UNORDERED")) {
			throw new IllegalArgumentException("Invalid type! " + type);
		}
		this.type = type;

		if (targetGroup == null) {
			throw new IllegalArgumentException(
					"Target group must not be null!");
		}
		for (int i=0; i<targetGroup.length; i++) {
			if (targetGroup[i].getPort() == -1) {
				throw new IllegalArgumentException("Port of " + targetGroup[i].getTargetID() + " must not be null!");
			}
		}
		this.targetGroup = targetGroup;
	}

	public BftsmartCommand getCommand() {
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
