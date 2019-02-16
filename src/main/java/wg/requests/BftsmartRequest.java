package wg.requests;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bftsmart.tom.ServiceProxy;
import wg.Execution.WorkloadExecutionException;
import wg.responses.BftsmartResponse;
import wg.responses.Response;
import wg.workload.Target;

public class BftsmartRequest extends Request implements Callable<Response[]> {

	private static final Logger log = LoggerFactory
			.getLogger(BftsmartRequest.class);

	private final BftsmartCommand command;
	private final String type;
	private final ServiceProxy[] clients;

	/**
	 * The time (in seconds) a BFTSMaRt client will wait for responses before
	 * returning null
	 */
	private static final int BFTSMaRt_TIMEOUT = 20;

	public BftsmartRequest(long numberOfClients, BftsmartCommand command,
			String type) {

		if (command == null) {
			throw new IllegalArgumentException("Command must not be null!");
		}
		this.command = command;

		if (type == null) {
			throw new IllegalArgumentException("Type must not be null!");
		}
		if (!type.toUpperCase().equals("ORDERED")
				&& !type.toUpperCase().equals("UNORDERED")) {
			throw new IllegalArgumentException("Invalid type! " + type);
		}
		this.type = type;

		this.clients = new ServiceProxy[(int) numberOfClients];
		// for (int i = 0; i < numberOfClients; i++) {
		// clients[i] = new ServiceProxy(i);
		// clients[i].setInvokeTimeout(BFTSMaRt_TIMEOUT);
		// }
	}

	private void setBftsmartHosts(Target[] targets)
			throws WorkloadExecutionException {
		try {
			FileWriter fw = new FileWriter("config/hosts.config");
			BufferedWriter bw = new BufferedWriter(fw);
			for (int j = 0; j < targets.length; j++) {
				bw.write(j + " ");
				bw.write(InetAddress.getByName(targets[j].getServerName())
						.getHostAddress() + " ");
				bw.write(String.valueOf(targets[j].getPort()));
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			throw new WorkloadExecutionException(
					"Error while setting BFTSMaRt hosts!", e);
		}
	}

	@Override
	public Response[] call() throws WorkloadExecutionException {

		setBftsmartHosts(getTargets());

		Response[] responses = new Response[clients.length];

		for (int i = 0; i < clients.length; i++) {
			responses[i] = executeSinlgeRequest(clients[i], getTargets());
		}

		return responses;
	}

	private Response executeSinlgeRequest(ServiceProxy client, Target[] targets)
			throws WorkloadExecutionException {
		byte[] reply = null;

		long startTime = System.currentTimeMillis();
		try {
			if (command.getType() == BftsmartCommandType.BYTE_ARRAY) {
				reply = executeByteArrayRequest(client);
			} else {
				reply = executeObjectStreamRequest(client);
			}
		} catch (RuntimeException e) {
			throw new WorkloadExecutionException(
					"Error while executing BFTSMaRt request!", e);
		}
		long endTime = System.currentTimeMillis();

		if (reply == null || reply.length == 0) {
			log.error("No reply received for BFTSMaRt request!");
			return null;
		}
		return new BftsmartResponse(startTime, endTime, targets, reply);

	}

	private byte[] executeObjectStreamRequest(ServiceProxy serviceProxy)
			throws WorkloadExecutionException {

		byte[] reply;
		Object[] objectsToServer = command.getObjects();
		ByteArrayOutputStream byteOut;
		try {
			byteOut = new ByteArrayOutputStream();
			ObjectOutput objOut = new ObjectOutputStream(byteOut);

			for (int i = 0; i < objectsToServer.length; i++) {
				objOut.writeObject(objectsToServer[i]);
			}

			objOut.flush();
			byteOut.flush();

		} catch (IOException e) {
			throw new WorkloadExecutionException(
					"Error while preparing BFTSMaRt request for sending!", e);
		}

		if (type.toUpperCase().equals("ORDERED")) {
			reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
		} else {
			reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
		}

		return reply;
	}

	private byte[] executeByteArrayRequest(ServiceProxy serviceProxy) {
		byte[] reply;
		byte[] content = command.getContent().getBytes();
		if (type.toUpperCase().equals("ORDERED")) {
			reply = serviceProxy.invokeOrdered(content);
		} else {
			reply = serviceProxy.invokeUnordered(content);
		}
		return reply;
	}

}
