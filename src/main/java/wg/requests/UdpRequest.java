package wg.requests;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wg.Execution.WorkloadExecutionException;
import wg.responses.Response;
import wg.responses.UdpResponse;
import wg.workload.Target;

public class UdpRequest extends Request implements Callable<Response[]> {

	private static final Logger log = LoggerFactory.getLogger(UdpRequest.class);

	private final String content;
	private DatagramSocket[] clients;

	public UdpRequest(JSONObject object) {
		
		super(object);

		String content = (String) object.get("content");
		if (content == null) {
			throw new IllegalArgumentException("Content must not be null!");
		}
		this.content = content;

		this.clients = new DatagramSocket[(int) getNumberOfClients()];
		for (int i = 0; i < getNumberOfClients(); i++) {
			try {
				clients[i] = new DatagramSocket();
			} catch (SocketException e) {
				log.error(e.getMessage());
			}
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

	private Response executeSingleRequest(DatagramSocket client, Target target)
			throws WorkloadExecutionException {
		String responseContent = null;
		long startTime = 0;
		long endTime = 0;
		try {

			client.setSoTimeout(30000);
			InetAddress IPAddress = InetAddress
					.getByName(target.getServerName());
			byte[] sendData = new byte[1024];
			byte[] receiveData = new byte[1024];
			sendData = content.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, IPAddress, target.getPort());
			startTime = System.currentTimeMillis();
			client.send(sendPacket);
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			client.receive(receivePacket);
			endTime = System.currentTimeMillis();
			responseContent = new String(receivePacket.getData());
			client.close();
		} catch (SocketTimeoutException e) {
			throw new WorkloadExecutionException(
					"UDP Verbindung kann nicht aufgebaut werden. Grund: Verbindung ist getimeouted.",
					e);

		} catch (IOException e) {
			throw new WorkloadExecutionException(
					"Error while executing UDP request!", e);
		}
		return new UdpResponse(startTime, endTime, target, responseContent);
	}

}
