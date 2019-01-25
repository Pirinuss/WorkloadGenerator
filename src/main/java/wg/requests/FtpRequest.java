package wg.requests;

import wg.workload.ProtocolType;
import wg.workload.Request;

public class FtpRequest extends Request {

	private final FtpMethodType method;
	private final String localResource;
	private final String remoteResource;
	private final String username;
	private final String password;

	public FtpRequest(String requestName, ProtocolType protocol, FtpMethodType method, String localResource,
			String remoteResource, String username, String password) {
		super(requestName, protocol);
		this.method = method;
		this.localResource = localResource;
		this.remoteResource = remoteResource;
		this.username = username;
		this.password = password;
	}

	public FtpMethodType getMethod() {
		return method;
	}

	public String getLocalResource() {
		return localResource;
	}

	public String getRemoteResource() {
		return remoteResource;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

}
