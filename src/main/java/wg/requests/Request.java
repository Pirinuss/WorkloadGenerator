package wg.requests;

import java.util.concurrent.Callable;

import wg.Execution.WorkloadExecutionException;
import wg.responses.Response;
import wg.workload.Target;

public class Request implements Callable<Response[]> {

	private Target[] targets;

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
