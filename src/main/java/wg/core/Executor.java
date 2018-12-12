package wg.core;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;

import wg.workload.EventDiscriptor;
import wg.workload.Frame;
import wg.workload.Workload;

public class Executor {
	
	ExecutorService exeService;

	public void executeWorkload(Workload w) {
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
		ArrayList<EventDiscriptor> executedEvents = null;
		Timestamp startTime = new Timestamp(System.currentTimeMillis());
		while (events.size() > 0) {
			executedEvents = new ArrayList<EventDiscriptor>();
			for (int i=0; i<events.size(); i++) {
				currentEvent = events.get(i);
				exeTime = currentEvent.getTime();
				Timestamp currentTime = new Timestamp(System.currentTimeMillis());
				long dif = currentTime.getTime()-startTime.getTime();
				if (currentTime.getTime()-exeTime >=startTime.getTime() ) {
					Event event = new Event();
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
					executedEvents.add(events.get(i));
				}
			}
			for (int j=0; j<executedEvents.size(); j++) {
				for (int x =0; x<events.size(); x++) {
					if (events.get(x).equals(executedEvents.get(j))) {
						events.remove(events.get(x));
					}
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
	
}
