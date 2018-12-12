package wg.core;

import java.util.concurrent.Callable;

import wg.requests.HttpRequest;
import wg.workload.Request;
import wg.workload.Target;

public class Event implements Callable<Response> {
	
	private Target target;
	private Request request;
	
	public Event(Target target, Request request) {
		this.target = target;
		this.request = request;
	}

	public Response call() throws Exception {
		Response response = new Response();
		switch(request.getProtocol()) {
		
		case HTTP:
			response = executeHttpEvent();
			break;
		case FTP:
			break;
		case TCP:
			break;
		case UDP:
			break;
		default:
			break;
		}
		response.setTest("Es klappt");
		return response;
	}
	
	private Response executeHttpEvent() {
		HttpRequest httpRequest = (HttpRequest) request;
		return null;
	}

}
