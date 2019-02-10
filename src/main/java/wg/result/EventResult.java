package wg.result;

import wg.core.Response;
import wg.workload.EventDescriptor;

public class EventResult {
	
	EventDescriptor event;
	Response response;
	int id;
	
	public EventResult(EventDescriptor event, Response response, int id) {
		this.event = event;
		this.response = response;
		this.id = id;
	}

	public EventDescriptor getEvent() {
		return event;
	}

	public Response getResponse() {
		return response;
	}

	public int getId() {
		return id;
	}
	
	
	
	

}
