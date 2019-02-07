package wg.responses;

import org.apache.http.HttpResponse;

import wg.core.Response;
import wg.workload.Target;

public class HttpResponseObject extends Response {

	private final HttpResponse response;

	public HttpResponseObject(long startTime, long endTime, Target target,
			HttpResponse response) {
		super(startTime, endTime, target);
		this.response = response;
	}

	public HttpResponse getResponse() {
		return response;
	}

}
