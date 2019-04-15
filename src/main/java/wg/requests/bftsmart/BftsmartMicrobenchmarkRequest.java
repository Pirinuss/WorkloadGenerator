package wg.requests.bftsmart;

import java.util.concurrent.Callable;

import org.json.simple.JSONObject;

import bftsmart.tom.ServiceProxy;
import wg.executor.WorkloadExecutionException;
import wg.parser.workload.Target;
import wg.requests.Request;
import wg.responses.BftsmartMicrobenchmarkResponse;
import wg.responses.Response;

public class BftsmartMicrobenchmarkRequest extends Request
		implements Callable<Response[]> {

//	private static final Logger log = LoggerFactory
//			.getLogger(BftsmartMicrobenchmarkRequest.class);

	private final boolean readOnly;
	private final int numberOfOperations;
	@SuppressWarnings("unused")
	private final int requestSize;
	private final int interval;

	public BftsmartMicrobenchmarkRequest(JSONObject object) {
		super(object);

		// Type
		String type = (String) object.get("type");
		if (type == null) {
			throw new IllegalArgumentException("Type must not be null!");
		}
		if (!type.toUpperCase().equals("ORDERED")
				&& !type.toUpperCase().equals("UNORDERED")) {
			throw new IllegalArgumentException("Invalid type! " + type);
		}
		readOnly = type.toUpperCase().equals("UNORDERED");

		// NumberOfOperations
		long opsNumber = (long) object.get("numberOfOperations");
		if (opsNumber == 0) {
			throw new IllegalArgumentException(
					"NumberOfOperations must not be null!");
		}
		numberOfOperations = (int) opsNumber;

		// RequestSize
		long requestNumber = (long) object.get("requestSize");
		if (requestNumber == 0) {
			throw new IllegalArgumentException("RequestSize must not be null!");
		}
		requestSize = (int) requestNumber;

		// Interval
		long intervalSize = (long) object.get("interval");
		if (intervalSize == 0) {
			throw new IllegalArgumentException("Interval must not be null!");
		}
		interval = (int) intervalSize;
	}

	@Override
	public Response[] call() throws WorkloadExecutionException {

		Response[] responses = new Response[(int) numberOfClients];

		for (int i = 0; i < numberOfClients; i++) {
			responses[i] = executeSinlgeRequest(i, targets);
		}

		return responses;
	}

	private Response executeSinlgeRequest(int clientIndex, Target[] targets) {

		ServiceProxy proxy = new ServiceProxy(clientIndex);
		byte[] reply = null;
		boolean failed = false;

		long startTime = System.currentTimeMillis();

		for (int i = 0; i < numberOfOperations; i++) {

			if (readOnly)
				reply = proxy.invokeUnordered(null);
			else
				reply = proxy.invokeOrdered(null);

			if (interval > 0) {
				try {
					// sleeps interval ms before sending next request
					Thread.sleep(interval);
				} catch (InterruptedException ex) {
				}
			}
		}

		long endTime = System.currentTimeMillis();

		proxy.close();

		failed = reply == null;

		return new BftsmartMicrobenchmarkResponse(startTime, endTime, targets,
				reply, failed);
	}

}
