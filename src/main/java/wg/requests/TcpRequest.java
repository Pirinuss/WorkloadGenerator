package wg.requests;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;

import wg.Execution.WorkloadExecutionException;
import wg.responses.Response;
import wg.responses.TcpResponse;
import wg.workload.Target;

public class TcpRequest extends Request implements Callable<Response[]> {

	private final String content;
	private Socket[] clients;

	public TcpRequest(long numberOfClients, String content) {

		if (content == null) {
			throw new IllegalArgumentException("Content must not be null!");
		}
		this.content = content;

		this.clients = new Socket[(int) numberOfClients];
		for (int i = 0; i < numberOfClients; i++) {
			clients[i] = new Socket();
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

	private Response executeSingleRequest(Socket client, Target target)
			throws WorkloadExecutionException {
		String responseContent = null;
		long startTime = System.currentTimeMillis();
		try {

			client.setSoTimeout(5000);
			DataOutputStream outToServer = new DataOutputStream(
					client.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(
					new InputStreamReader(client.getInputStream()));
			outToServer.writeBytes(content);
			responseContent = inFromServer.readLine();
			client.close();
		} catch (SocketTimeoutException e) {
			throw new WorkloadExecutionException("TCP connection timed out!",
					e);
		} catch (ConnectException e) {
			throw new WorkloadExecutionException(
					"Could not estabilish TCP connection!", e);
		} catch (IOException e) {
			throw new WorkloadExecutionException(
					"Error while executing TCP request!", e);
		}
		long endTime = System.currentTimeMillis();
		return new TcpResponse(startTime, endTime, target, responseContent);
	}

}
