package wg.core;

import java.util.ArrayList;

import wg.workload.EventDescriptor;
import wg.workload.Request;
import wg.workload.Target;

public class Result {
	
	ArrayList<ResultObject> results = new ArrayList<ResultObject>();
	int framesNumber = 1;
	
	public void safeResponse(ResultObject resultObject) {
		results.add(resultObject);
		for (int i=0; i<results.size(); i++) {
			String frameName = results.get(i).getFrame().getFrameName();
			if (!resultObject.getFrame().getFrameName().equals(frameName)) {
				framesNumber++;
			}
		}
	}
	
	public void printResponses() {
		for (int i=0; i<framesNumber; i++) {
			String frameName = "frame" + framesNumber;
			System.out.println("---Ergebnisse f�r Frame " + framesNumber + " ---");
			System.out.println();
			for (int j=0; j<results.size(); j++) {
				if (results.get(j).getFrame().getFrameName().equals(frameName)) {
					EventDescriptor eventDis = results.get(j).getEvent();
					Response response = results.get(j).getResponse();
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
						System.out.println("No response found for event: " + results.get(j).getEvent().getEventName());
					}
				}
			}
		}
	}
	
}
