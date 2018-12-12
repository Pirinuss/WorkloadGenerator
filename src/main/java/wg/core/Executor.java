package wg.core;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;

import wg.workload.EventDiscriptor;
import wg.workload.Frame;
import wg.workload.Request;
import wg.workload.Target;
import wg.workload.Workload;

public class Executor {
	
	ExecutorService exeService;
	Workload w;

	public void executeWorkload(Workload w) {
		this.w = w;
		ResponseStorage responseStorage;
		responseStorage = ResponseStorage.getInstance();
		Frame[] frames = w.getSchedule().getFrames();
		int threadPoolSize = frames.length;
		exeService = Executors.newFixedThreadPool(threadPoolSize);
		for (int i=0; i<frames.length; i++) {
			Response[] responses = executeFrame(frames[i]);
			for (int j=0; j<responses.length; j++) {
				EventDiscriptor event = frames[i].getEventDisriptorByName("event"+j);
				Response response = responses[j];
				responseStorage.safeResponse(frames[i], event, response);
			}
		}
		exeService.shutdown();
	}
	
	private Response[] executeFrame(Frame f) {
		ArrayList<EventDiscriptor> events = new ArrayList<EventDiscriptor>(Arrays.asList(f.getEvents()));
		EventDiscriptor currentEvent;
		long exeTime;
		Response[] responses = new Response[events.size()];
		Future[] futures = new Future[events.size()];
		Timestamp startTime = new Timestamp(System.currentTimeMillis());
		while (events.size() > 0) {
			for (int i=0; i<events.size(); i++) {
				currentEvent = events.get(i);
				exeTime = currentEvent.getTime();
				Timestamp currentTime = new Timestamp(System.currentTimeMillis());
				if (currentTime.getTime()-exeTime >=startTime.getTime() ) {
					Target target = mapTarget(currentEvent.getTargetName());
					Request request = mapRequest(currentEvent.getRequestName());
					Event event = new Event(target, request);
					Future<Response> response = exeService.submit(event);
					//***DEBUG
					System.out.println("---NEUES EVENT---");
					try {
						System.out.println("New Response: " + exeTime + " " + f.getFrameName() + " " + currentEvent.getEventName());
						Response response1;
						response1 = (Response) futures[i].get();
					} catch (Exception e) {
						System.out.println("Response ist null: " + exeTime + " " + f.getFrameName());
						
					}
					//***DEBUGENDE
					futures[i] = response;
					events.remove(currentEvent);
					i--;
				}
			}
		}
		for (int i = 0; i<futures.length; i++) {
			try {
				Response response = (Response) futures[i].get();
				responses[i] = response;
			} catch (Exception e) {
				
			}
		}
		return responses;
	}
	
	private Target mapTarget(String targetName) {
		HashMap<String, Target> targets = w.getTargets();
		Target target = targets.get(targetName);
		return target;
	}
	
	private Request mapRequest(String requestName) {
		HashMap<String, Request> requests = w.getRequests();
		Request request = requests.get(requestName);
		return request;
	}
	
}
