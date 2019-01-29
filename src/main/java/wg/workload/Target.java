package wg.workload;

public class Target {

	private final String targetName;
	private final String serverName;
	private final String port;

	public Target(String targetName, String serverName, String port) {
		this.targetName = targetName;
		this.serverName = serverName;
		this.port = port;
		if (serverName == null) {
			throw new IllegalArgumentException("No servername found for " + targetName);
		}
	}

	public String getTargetName() {
		return targetName;
	}

	public String getServerName() {
		return serverName;
	}

	public String getPort() {
		return port;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(targetName);
		sb.append(System.getProperty("line.separator"));
		sb.append(serverName);
		sb.append(System.getProperty("line.separator"));
		sb.append(port);
		sb.append(System.getProperty("line.separator"));
		return sb.toString();
	}

}
