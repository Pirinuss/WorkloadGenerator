package wg.requests.bftsmart;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.Callable;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.ServiceProxy;
import wg.executor.WorkloadExecutionException;
import wg.parser.WorkloadParserException;
import wg.parser.workload.Target;
import wg.requests.Request;
import wg.responses.BftsmartResponse;
import wg.responses.Response;

public class BftsmartRequest extends Request implements Callable<Response[]> {

	private static final Logger log = LoggerFactory
			.getLogger(BftsmartRequest.class);

	private final BftsmartCommand command;
	private final String type;
	private final boolean isSynch;
	private final ServiceProxy[] synchClients;
	private final AsynchServiceProxy[] asynchClients;

	/**
	 * The time (in milliseconds) a BFTSMaRt client will wait for responses before
	 * returning null
	 */
	private static final int BFTSMaRt_TIMEOUT = 2000;

	public BftsmartRequest(JSONObject object) throws WorkloadParserException {

		super(object);

		// Command specification
		JSONObject commandObj = (JSONObject) object.get("command");
		if (commandObj == null) {
			throw new IllegalArgumentException("Command must not be null!");
		}
		BftsmartCommand command = new BftsmartCommand(commandObj);
		this.command = command;

		// Type
		String type = (String) object.get("type");
		if (type == null) {
			throw new IllegalArgumentException("Type must not be null!");
		}
		if (!type.toUpperCase().equals("ORDERED")
				&& !type.toUpperCase().equals("UNORDERED")) {
			throw new IllegalArgumentException("Invalid type! " + type);
		}
		this.type = type;

		// Client specification
		String clientType = (String) object.get("clientType");
		if (clientType == null) {
			this.isSynch = true;
		} else {
			if (clientType == null || (!clientType.equals("asynchron")
					&& !clientType.equals("synchron"))) {
				throw new IllegalArgumentException(
						"Invalid BFTSMaRt client type! " + clientType);
			}
			this.isSynch = clientType.equals("synchron");
		}

		// Initialize clients
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

		setBftsmartHosts(targets);

		Response[] responses = new Response[(int) numberOfClients];

		for (int i = 0; i < numberOfClients; i++) {
			responses[i] = executeSinlgeRequest(i, targets);
		}

		return responses;
	}

	private Response executeSinlgeRequest(int clientIndex, Target[] targets)
			throws WorkloadExecutionException {
		
		boolean failed = false;

		byte[] reply = null;

		long startTime = System.currentTimeMillis();
		try {
			byte[] byteOut = command.getByteOut().toByteArray();

			if (type.toUpperCase().equals("ORDERED")) {
				if (isSynch) {
					reply = synchClients[clientIndex].invokeOrdered(byteOut);
				} else {
					reply = asynchClients[clientIndex].invokeOrdered(byteOut);
				}
			} else {
				if (isSynch) {
					reply = synchClients[clientIndex].invokeUnordered(byteOut);
				} else {
					reply = asynchClients[clientIndex].invokeUnordered(byteOut);
				}
			}
		} catch (RuntimeException e) {
			throw new WorkloadExecutionException(
					"Error while executing BFTSMaRt request!", e);
		}
		long endTime = System.currentTimeMillis();

		if (reply == null || reply.length == 0) {
			log.error("No reply received for BFTSMaRt request!");
			failed = true;
			reply = null;
		} else {
			System.out.println(new String(reply));
		}
		

		return new BftsmartResponse(startTime, endTime, targets, reply, failed);

	}

}
