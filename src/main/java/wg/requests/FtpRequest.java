package wg.requests;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;

import wg.core.Response;
import wg.core.WorkloadExecutionException;
import wg.responses.FtpResponse;
import wg.workload.ProtocolType;
import wg.workload.Request;
import wg.workload.Target;

public class FtpRequest extends Request implements RequestInterface {

	private final FtpMethodType method;
	private final String localResource;
	private final String remoteResource;
	private final String username;
	private final String password;
	private FTPClient[] clients;

	public FtpRequest(String requestName, ProtocolType protocol,
			long numberOfClients, FtpMethodType method, String localResource,
			String remoteResource, String username, String password) {

		super(requestName, protocol, numberOfClients);

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

		this.clients = new FTPClient[(int) numberOfClients];
		for (int i = 0; i < numberOfClients; i++) {
			clients[i] = new FTPClient();
		}
	}

	@Override
	public Response[] execute(Target[] targets)
			throws WorkloadExecutionException {

		Response[] responses = new Response[clients.length * targets.length];

		int index = 0;
		for (int i = 0; i < clients.length; i++) {
			for (int j = 0; j < targets.length; j++) {
				responses[index] = executeSingleRequest(clients[i], targets[j]);
				index++;
			}
		}

		return responses;
	}

	private Response executeSingleRequest(FTPClient client, Target target)
			throws WorkloadExecutionException {
		long startTime = System.currentTimeMillis();
		int replyCode = 0;
		try {
			// TODO FTPConnectionClosedException abfangen (passiert wenn FTP
			// response = 421)
			client.connect(target.getServerName(), target.getPort());
			client.login(username, password);
			if (method == FtpMethodType.GET) {
				FileOutputStream fos = new FileOutputStream(localResource);
				client.retrieveFile(remoteResource, fos);
				replyCode = client.getReplyCode();
				fos.close();
			}
			if (method == FtpMethodType.PUT) {
				FileInputStream fis = new FileInputStream(localResource);
				client.storeFile(remoteResource, fis);
				replyCode = client.getReplyCode();
				fis.close();
			}
			client.logout();
			client.disconnect();
		} catch (IOException e) {
			throw new WorkloadExecutionException(
					"Error while executing FTP request!", e);
		}
		long endTime = System.currentTimeMillis();
		return new FtpResponse(startTime, endTime, target, replyCode);
	}

}
