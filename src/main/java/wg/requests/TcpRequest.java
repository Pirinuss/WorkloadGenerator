package wg.requests;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;

import wg.core.Response;
import wg.core.WorkloadExecutionException;
import wg.responses.TcpUdpResponse;
import wg.workload.ProtocolType;
import wg.workload.Request;
import wg.workload.Target;

public class TcpRequest extends Request implements RequestInterface {

	private final String content;
	private Socket[] clients;

	public TcpRequest(String requestName, ProtocolType protocol,
			long numberOfClients, String content) {

		super(requestName, protocol, numberOfClients);

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
		return new TcpUdpResponse(startTime, endTime, target, responseContent);
	}

}
