package wg.core;

import java.util.HashMap;

import wg.workload.EventDiscriptor;
import wg.workload.Frame;

public class ResponseStorage {
	
	private static ResponseStorage instance;
	HashMap<Frame, HashMap<EventDiscriptor,Response>> responses = new HashMap<Frame, HashMap<EventDiscriptor,Response>>();
	
	private ResponseStorage() {}

	  public static ResponseStorage getInstance() {
	    if (instance == null) {
	      instance = new ResponseStorage();
	      return instance;
	    } else {
	      return instance;
	    }
	  }
	
	public void safeResponse(Frame frame, EventDiscriptor event, Response response) {
		HashMap<EventDiscriptor, Response> hm = new HashMap<EventDiscriptor, Response>();
		hm.put(event, response);
		responses.put(frame, hm);
	}
	
}
