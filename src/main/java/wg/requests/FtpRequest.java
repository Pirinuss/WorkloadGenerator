package wg.requests;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wg.Execution.WorkloadExecutionException;
import wg.responses.FtpResponse;
import wg.responses.Response;
import wg.workload.Target;

public class FtpRequest extends Request implements Callable<Response[]> {

	private static final Logger log = LoggerFactory.getLogger(FtpRequest.class);

	private final FtpMethodType method;
	private final String localResource;
	private final String remoteResource;
	private final String username;
	private final String password;
	private FTPClient[] clients;

	/**
	 * The time (in milliseconds) a FTP client will wait for responses before
	 * returning null
	 */
	private static final int TIMEOUT = 20;

	public FtpRequest(long numberOfClients, FtpMethodType method,
			String localResource, String remoteResource, String username,
			String password) {

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
			FTPClient client = new FTPClient();
			client.setDefaultTimeout(TIMEOUT);
			clients[i] = new FTPClient();
		}
	}

	@Override
	public Response[] call() throws WorkloadExecutionException {

		Response[] responses = new Response[clients.length
				* getTargets().length];

		int index = 0;
		for (int i = 0; i < clients.length; i++) {
			for (int j = 0; j < getTargets().length; j++) {
				responses[index] = executeSingleRequest(clients[i],
						getTargets()[j]);
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
			log.error(e.getMessage());
			throw new WorkloadExecutionException(
					"Error while executing FTP request!", e);
		}

		long endTime = System.currentTimeMillis();
		return new FtpResponse(startTime, endTime, target, replyCode);
	}

}
