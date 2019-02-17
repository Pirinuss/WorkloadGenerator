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

import bftsmart.tom.AsynchServiceProxy;
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
	private final boolean isSynch;
	private final long numberOfClients;
	private final ServiceProxy[] synchClients;
	private final AsynchServiceProxy[] asynchClients;

	/**
	 * The time (in seconds) a BFTSMaRt client will wait for responses before
	 * returning null
	 */
	private static final int BFTSMaRt_TIMEOUT = 20;

	public BftsmartRequest(long numberOfClients, BftsmartCommand command,
			String type, String clientType) {

		if (numberOfClients < 1) {
			throw new IllegalArgumentException("At least one client required!");
		}
		this.numberOfClients = numberOfClients;

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

		if (clientType == null || (!clientType.equals("asynchron")
				&& !clientType.equals("synchron"))) {
			log.error("Invalid BFTSMaRt client type");
			throw new IllegalArgumentException("Invalid BFTSMaRt client type!");
		}
		this.isSynch = clientType.equals("synchron");

		if (isSynch) {
			this.synchClients = new ServiceProxy[(int) numberOfClients];
			this.asynchClients = null;
			for (int i = 0; i < numberOfClients; i++) {
				synchClients[i] = new ServiceProxy(i);
				synchClients[i].setInvokeTimeout(BFTSMaRt_TIMEOUT);
			}
		} else {
			this.asynchClients = new AsynchServiceProxy[(int) numberOfClients];
			this.synchClients = null;
			for (int j = 0; j < numberOfClients; j++) {
				asynchClients[j] = new AsynchServiceProxy(j);
				asynchClients[j].setInvokeTimeout(BFTSMaRt_TIMEOUT);
			}
		}

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

		Response[] responses = new Response[(int) numberOfClients];

		for (int i = 0; i < numberOfClients; i++) {
			responses[i] = executeSinlgeRequest(i, getTargets());
		}

		return responses;
	}

	private Response executeSinlgeRequest(int clientIndex, Target[] targets)
			throws WorkloadExecutionException {
		byte[] reply = null;

		long startTime = System.currentTimeMillis();
		try {
			if (command.getType() == BftsmartCommandType.BYTE_ARRAY) {
				reply = executeByteArrayRequest(clientIndex);
			} else {
				reply = executeObjectStreamRequest(clientIndex);
			}
		} catch (RuntimeException e) {
			throw new WorkloadExecutionException(
					"Error while executing BFTSMaRt request!", e);
		}
		long endTime = System.currentTimeMillis();

		if (reply == null || reply.length == 0) {
			log.error("No reply received for BFTSMaRt request!");
			reply = null;
		}
		return new BftsmartResponse(startTime, endTime, targets, reply);

	}

	private byte[] executeObjectStreamRequest(int clientIndex)
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
			if (isSynch) {
				reply = synchClients[clientIndex]
						.invokeOrdered(byteOut.toByteArray());
			} else {
				reply = asynchClients[clientIndex]
						.invokeOrdered(byteOut.toByteArray());
			}
		} else {
			if (isSynch) {
				reply = synchClients[clientIndex]
						.invokeUnordered(byteOut.toByteArray());
			} else {
				reply = asynchClients[clientIndex]
						.invokeUnordered(byteOut.toByteArray());
			}
		}

		return reply;
	}

	private byte[] executeByteArrayRequest(int clientIndex) {
		byte[] reply;
		byte[] content = command.getContent().getBytes();
		if (type.toUpperCase().equals("ORDERED")) {
			if (isSynch) {
				reply = synchClients[clientIndex].invokeOrdered(content);
			} else {
				reply = asynchClients[clientIndex].invokeOrdered(content);
			}
		} else {
			if (isSynch) {
				reply = synchClients[clientIndex].invokeUnordered(content);
			} else {
				reply = asynchClients[clientIndex].invokeUnordered(content);
			}
		}
		return reply;
	}

}
