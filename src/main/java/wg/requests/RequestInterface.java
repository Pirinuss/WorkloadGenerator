package wg.requests;

import wg.core.Response;
import wg.core.WorkloadExecutionException;
import wg.workload.Target;

public interface RequestInterface {

	public Response[] execute(Target[] target) throws WorkloadExecutionException;
	
}
