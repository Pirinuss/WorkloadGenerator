package wg.requests;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import wg.core.Response;
import wg.core.WorkloadExecutionException;
import wg.responses.TcpUdpResponse;
import wg.workload.ProtocolType;
import wg.workload.Request;
import wg.workload.Target;

public class UdpRequest extends Request implements RequestInterface {

	private final String content;
	private DatagramSocket[] clients;

	public UdpRequest(String requestName, ProtocolType protocol,
			long numberOfClients, String content) {

		super(requestName, protocol, numberOfClients);

		if (content == null) {
			throw new IllegalArgumentException("Content must not be null!");
		}
		this.content = content;

		this.clients = new DatagramSocket[(int) numberOfClients];
		for (int i = 0; i < numberOfClients; i++) {
			try {
				clients[i] = new DatagramSocket();
			} catch (SocketException e) {
				// ignore
			}
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
		return new TcpUdpResponse(startTime, endTime, target, responseContent);
	}

}
