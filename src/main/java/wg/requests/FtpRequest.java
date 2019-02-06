package wg.requests;

import org.apache.commons.net.ftp.FTPClient;

import wg.workload.ProtocolType;
import wg.workload.Request;

public class FtpRequest extends Request {

	private final FtpMethodType method;
	private final String localResource;
	private final String remoteResource;
	private final String username;
	private final String password;
	private FTPClient client;

	public FtpRequest(String requestName, ProtocolType protocol,
			FtpMethodType method, String localResource, String remoteResource,
			String username, String password) {

		super(requestName, protocol);

		if (method == null) {
			throw new IllegalArgumentException("Method must not be null!");
		}
		this.method = method;

		if (localResource == null) {
			throw new IllegalArgumentException(
					"Local resource must not be null");
		}
		this.localResource = localResource;

		if (remoteResource == null) {
			throw new IllegalArgumentException(
					"Remote resource must not be null");
		}
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

	public FTPClient getClient() {
		return client;
	}

	public void setClient(FTPClient client) {
		this.client = client;
	}

}
