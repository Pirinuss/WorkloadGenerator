package wg.requests;

import java.util.concurrent.Callable;

import org.json.simple.JSONObject;

import wg.executor.WorkloadExecutionException;
import wg.parser.workload.Target;
import wg.responses.Response;

public abstract class Request implements Callable<Response[]> {

	protected final long numberOfClients;
	protected Target[] targets;

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
	
	public void setTargets(Target[] targets) {
		this.targets = targets;
	}

	@Override
	public abstract Response[] call() throws WorkloadExecutionException;

}
