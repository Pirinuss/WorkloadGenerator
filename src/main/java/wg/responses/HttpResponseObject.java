package wg.responses;

import org.apache.http.HttpResponse;

import wg.workload.Target;

public class HttpResponseObject extends Response {

	private final HttpResponse response;

	public HttpResponseObject(long startTime, long endTime, Target target,
			HttpResponse response, boolean failed) {
		super(startTime, endTime, target, failed);
		this.response = response;
	}

	@Override
	public void print() {
		// @formatter:off
		System.out.println("     Target: " + targetGroup[0].getServerName());
		System.out.println("     Execution time: " + getRTT());
		System.out.println("     Response code: " + response.getStatusLine());
		System.out.println("     Response length: " + response.getEntity().getContentLength());
		// @formatter:on
	}

}
