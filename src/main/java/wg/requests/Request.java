package wg.requests;

import java.util.concurrent.Callable;

import org.json.simple.JSONObject;

import wg.Execution.WorkloadExecutionException;
import wg.responses.Response;
import wg.workload.Target;

public class Request implements Callable<Response[]> {

	private final long numberOfClients;
	private Target[] targets;

	public Request(JSONObject object) {
		if (object.get("numberOfClients") != null) {
			this.numberOfClients = (long) object.get("numberOfClients");
			if (numberOfClients < 1) {
				throw new IllegalArgumentException("At least one client required!");
			}
		} else {
			this.numberOfClients = 1;
		}
	}
	
	public long getNumberOfClients() {
		return numberOfClients;
	}
	
	public void setTargets(Target[] targets) {
		this.targets = targets;
	}

	public Target[] getTargets() {
		return targets;
	}

	@Override
	public Response[] call() throws WorkloadExecutionException {
		return null;
	}

}
