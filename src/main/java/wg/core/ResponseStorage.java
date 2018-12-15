package wg.core;

import java.util.HashMap;
import java.util.Map.Entry;

import wg.workload.EventDiscriptor;
import wg.workload.Frame;
import wg.workload.Request;
import wg.workload.Target;

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
		for (Entry<Frame, HashMap<EventDiscriptor, Response>> entry : responses.entrySet()) {
			Frame f = entry.getKey();
			if (frame.equals(f)) {
				HashMap<EventDiscriptor, Response> map = entry.getValue();
				map.put(event, response);
				return;
			}
		}
		HashMap<EventDiscriptor, Response> hm = new HashMap<EventDiscriptor, Response>();
		hm.put(event, response);
		responses.put(frame, hm);
	}
	
	public void printResponses() {
		for (Entry<Frame, HashMap<EventDiscriptor, Response>> e : responses.entrySet()) {
			Frame f = e.getKey();
			HashMap<EventDiscriptor,Response> map = e.getValue();
			System.out.println("---Ergebnisse für Frame: " + f.getFrameName() + " ---");
			System.out.println();
			for (Entry<EventDiscriptor, Response> entry : map.entrySet()) {
				EventDiscriptor eventDis = entry.getKey();
				Response response = entry.getValue();
				if (response != null) {
					long exeTime = response.getEventStopTime() - response.getEventStartTime();
					Target target = response.getTarget();
					String port = null;
					if (target.getPort() != null) {
						port = target.getPort();
					}
					Request request = response.getRequest();
					System.out.println("   Event: " + eventDis.getEventName());
					System.out.println("     Target: " + target.getServerName() + ", "+ port );
					System.out.println("     Request: " + request.getProtocol() );
					System.out.println("     Execution time: " + exeTime );
					System.out.println("     Response code: " + response.getResponseInfos());
					if (response.getResponseContent() != null) {
						System.out.println("     Response length: " + response.getResponseContent().length());
					} else {
						System.out.println("     Response length: 0");
					}
				} else {
					System.out.println("No response found for event: " + entry.getKey().getEventName());
				}
				
			}
		}
	}
	
}
