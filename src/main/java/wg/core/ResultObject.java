package wg.core;

import wg.workload.EventDiscriptor;
import wg.workload.Frame;

public class ResultObject {
	
	Frame frame;
	EventDiscriptor event;
	Response response;
	int id;
	
	public ResultObject(Frame frame, EventDiscriptor event, Response response, int id) {
		this.frame = frame;
		this.event = event;
		this.response = response;
		this.id = id;
	}

	public Frame getFrame() {
		return frame;
	}

	public EventDiscriptor getEvent() {
		return event;
	}

	public Response getResponse() {
		return response;
	}

	public int getId() {
		return id;
	}
	
	
	
	

}
