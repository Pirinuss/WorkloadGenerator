package wg.workload;

public class Target {
	
	private String targetName;
	private String serverName;
	private String port;
	
	public String getTargetName() {
		return targetName;
	}
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}
	
	public String getServerName() {
		return serverName;
	}
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	
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
