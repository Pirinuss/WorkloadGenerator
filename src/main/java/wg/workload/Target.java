package wg.workload;

public class Target {

	private static final int MIN_PORT = 0;
	private static final int MAX_PORT = 65535;

	private final String targetID;
	private final String serverName;
	private final int port;

	public Target(String targetID, String serverName, long port) {
		if (targetID == null) {
			throw new IllegalArgumentException("Target id must not be null!");
		}
		this.targetID = targetID;

		if (serverName == null) {
			throw new IllegalArgumentException("Servername must not be null!");
		}
		this.serverName = serverName;

		if (port != -1) {
			if (port < MIN_PORT || port > MAX_PORT) {
				throw new IllegalArgumentException("Invalid port number!");
			}
		}
		this.port = (int) port;
	}

	public String getTargetID() {
		return targetID;
	}

	public String getServerName() {
		return serverName;
	}

	public int getPort() {
		return port;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(targetID);
		sb.append(System.getProperty("line.separator"));
		sb.append(serverName);
		sb.append(System.getProperty("line.separator"));
		sb.append(port);
		sb.append(System.getProperty("line.separator"));
		return sb.toString();
	}

}
