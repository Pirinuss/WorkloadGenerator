package wg.core;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.sun.tools.javac.util.List;

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
		//TODO ThreadPool nicht mit MagicNumber initialisieren
		int threadPoolSize = 10;
		exeService = Executors.newFixedThreadPool(threadPoolSize);
		for (int i=0; i<frames.length; i++) {
			Response[] responses = executeFrame(frames[i]);
			for (int j=0; j<responses.length; j++) {
				EventDiscriptor event = frames[i].getEventDisriptorByName("event"+(j+1));
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
		ArrayList<Future> futures = new ArrayList<Future>();
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
					System.out.println("Event: " + currentEvent.getEventName() +  " ausgeführt um: " + dif);
					Target target = mapTarget(currentEvent.getTargetName());
					Request request = mapRequest(currentEvent.getRequestName());
					Event event = new Event(target, request);
					Future<Response> response = exeService.submit(event);
					futures.add(response);
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
		for (int i = 0; i<futures.size(); i++) {
			Response response = null;
			try {
				response = (Response) futures.get(i).get();
			} catch (Exception e) {
				e.printStackTrace();
			}
			responses[i] = response;
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
