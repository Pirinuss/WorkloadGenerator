package wg.requests;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wg.Execution.WorkloadExecutionException;
import wg.responses.Response;
import wg.responses.TcpResponse;
import wg.workload.Target;

public class TcpRequest extends Request implements Callable<Response[]> {

	/** The time (in milliseconds) a TCP Socket will wait for responses **/
	private static final int TIMEOUT = 5000;

	private static final Logger log = LoggerFactory.getLogger(TcpRequest.class);

	private final String content;

	public TcpRequest(JSONObject object) {

		super(object);

		String content = (String) object.get("content");
		if (content == null) {
			throw new IllegalArgumentException("Content must not be null!");
		}
		this.content = content;
	}

	@Override
	public Response[] call() throws WorkloadExecutionException {

		Response[] responses = new Response[(int) (numberOfClients
				* targets.length)];

		int index = 0;
		for (int i = 0; i < numberOfClients; i++) {
			for (int j = 0; j < targets.length; j++) {
				responses[index] = executeSingleRequest(targets[j]);
				index++;
			}
		}

		return responses;
	}

	private Response executeSingleRequest(Target target)
			throws WorkloadExecutionException {

		String responseContent = null;
		boolean failed = false;

		long startTime = System.currentTimeMillis();

		try {
			InetAddress addr = InetAddress.getByName(target.getServerName());
			Socket client = new Socket(addr, target.getPort());
			client.setSoTimeout(TIMEOUT);

			DataOutputStream outToServer = new DataOutputStream(
					client.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(
					new InputStreamReader(client.getInputStream()));
			outToServer.writeBytes(content);
			responseContent = inFromServer.readLine();

			client.close();
		} catch (SocketTimeoutException e) {
			responseContent = "Socket timed out";
			log.error(responseContent);
			failed = true;
		} catch (ConnectException e) {
			responseContent = "Could not establish TCP connection";
			log.error(responseContent);
			failed = true;
		} catch (UnknownHostException e) {
			responseContent = "Invalid servername";
			log.error(responseContent);
			failed = true;
		} catch (IOException e) {
			throw new WorkloadExecutionException(
					"Error while executing TCP request!", e);
		}
		long endTime = System.currentTimeMillis();
		return new TcpResponse(startTime, endTime, target, responseContent,
				failed);
	}

}
