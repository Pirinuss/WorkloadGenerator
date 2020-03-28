package wg.responses;

import java.net.URI;

import org.apache.http.HttpResponse;

import wg.parser.workload.Target;

public class HttpResponseObject extends Response {

	private final HttpResponse response;
	private final URI uri;

	public HttpResponseObject(long startTime, long endTime, Target target,
			HttpResponse response, boolean failed, URI uri) {
		super(startTime, endTime, target, failed);
		this.response = response;
		this.uri = uri;
	}

	@Override
	public void print() {
		// @formatter:off
		System.out.println("     Target: " + uri.toString());
		System.out.println("     Execution time: " + getRTT());
		System.out.println("     Response code: " + response.getStatusLine());
		// @formatter:on
	}

}
