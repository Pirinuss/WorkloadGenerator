package wg.workload;

public class Target {

	private final String targetName;
	private final String serverName;
	private final int port;

	public Target(String targetName, String serverName, int port) {
		if (targetName == null) {
			throw new IllegalArgumentException("Id must not be null!");
		}
		this.targetName = targetName;
		this.serverName = serverName;
		this.port = port;
		if (serverName == null) {
			throw new IllegalArgumentException("No servername found for ");
		}
	}

	public String getTargetName() {
		return targetName;
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
		sb.append(targetName);
		sb.append(System.getProperty("line.separator"));
		sb.append(serverName);
		sb.append(System.getProperty("line.separator"));
		sb.append(port);
		sb.append(System.getProperty("line.separator"));
		return sb.toString();
	}

}
