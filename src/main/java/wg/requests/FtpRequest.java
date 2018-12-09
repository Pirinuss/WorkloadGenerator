package wg.requests;

public class FtpRequest {

	private FtpMethodType method;
	private String localResource;
	private String remoteResource;
	private String username;
	private String password;
	
	public FtpMethodType getMethod() {
		return method;
	}
	public void setMethod(FtpMethodType method) {
		this.method = method;
	}
	public String getLocalResource() {
		return localResource;
	}
	public void setLocalResource(String localResource) {
		this.localResource = localResource;
	}
	public String getRemoteResource() {
		return remoteResource;
	}
	public void setRemoteResource(String remoteResource) {
		this.remoteResource = remoteResource;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	
}
