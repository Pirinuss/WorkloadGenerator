package wg.core;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import wg.workload.EventDiscriptor;
import wg.workload.Frame;
import wg.workload.Request;
import wg.workload.Target;
import wg.workload.Workload;

public class Executor {
	
	ExecutorService exeService;
	Workload w;
	ArrayList<EventDiscriptor> executedEvents;

	/**
	 * Executes the workload: Extracts the frames of the workload, calls the execution
	 * for each one and returns the result 
	 * @param w The workload that gets executed
	 * @return result The results of the workloads execution
	 */
	public Result executeWorkload(Workload w) {
		this.w = w;
		Result result = new Result();
		Frame[] frames = w.getSchedule().getFrames();
		//TODO ThreadPool nicht mit MagicNumber initialisieren
		int threadPoolSize = 10;
		exeService = Executors.newFixedThreadPool(threadPoolSize);
		for (int i=0; i<frames.length; i++) {
			Response[] responses = executeFrame(frames[i]);
			for (int j=0; j<responses.length; j++) {
				EventDiscriptor event = executedEvents.get(j);
				Response response = responses[j];
				ResultObject resultObject = new ResultObject(frames[i], event, response, j);
				result.safeResponse(resultObject);
			}
		}
		exeService.shutdown();
		return result;
	}
	
	/**
	 * Executes a frame: Extracts the events from the frame, calls the execution for
	 * each event by the right time and returns the responses
	 * @param f The frame that gets executed
	 * @return responses The array which contains all the responses of the
	 * executed events
	 */
	private Response[] executeFrame(Frame f) {
		ArrayList<EventDiscriptor> events = new ArrayList<EventDiscriptor>(Arrays.asList(f.getEvents()));
		EventDiscriptor currentEvent;
		long exeTime;
		long repetitions;
		long size = getTotalEventsNumber(events);
		Response[] responses = new Response[(int) size];
		ArrayList<Future<Response>> futures = new ArrayList<Future<Response>>();
		executedEvents = new ArrayList<EventDiscriptor>();
		Timestamp startTime = new Timestamp(System.currentTimeMillis());
		while (events.size() > 0) {
			for (int i=0; i<events.size(); i++) {
				currentEvent = events.get(i);
				repetitions = currentEvent.getRepetitions();
				exeTime = currentEvent.getTime();
				Timestamp currentTime = new Timestamp(System.currentTimeMillis());
				long dif = currentTime.getTime()-startTime.getTime();
				if (currentTime.getTime()-exeTime >=startTime.getTime() ) {
					for (int r=0; r<repetitions; r++) {
						System.out.println("Event: " + currentEvent.getEventName() +  " ausgeführt um: " + dif);
						Future<Response> response = executeEvent(currentEvent);
						futures.add(response);
					}
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
	
	/**
	 * Maps the event discription to an event object and executes it.
	 * Returns the response as a future object.
	 * @param currentEvent The event that gets mapped
	 * @return response The response of the event as a future object
	 */
	private Future<Response> executeEvent(EventDiscriptor currentEvent) {
		Target target = mapTarget(currentEvent.getTargetName());
		Request request = mapRequest(currentEvent.getRequestName());
		Event event = new Event(target, request);
		Future<Response> response = exeService.submit(event);
		executedEvents.add(currentEvent);
		return response;
	}
	
	/**
	 * Returns the target object for a target name
	 * @param targetName The namen of the target object
	 * @return target The target object
	 */
	private Target mapTarget(String targetName) {
		HashMap<String, Target> targets = w.getTargets();
		Target target = targets.get(targetName);
		return target;
	}
	
	/**
	 * Returns the request object for a request name
	 * @param requesttName The namen of the request object
	 * @return request The request object
	 */
	private Request mapRequest(String requestName) {
		HashMap<String, Request> requests = w.getRequests();
		Request request = requests.get(requestName);
		return request;
	}
	
	/**
	 * Returns the number (including repetitions) of events that have to 
	 * be executed
	 * @param events The list of events from the frame
	 * @return The number of events for a frame
	 */
	private long getTotalEventsNumber(ArrayList<EventDiscriptor> events) {
		long counter = 0;
		for (int i=0; i<events.size(); i++) {
			long reps = events.get(i).getRepetitions();
			counter = counter + reps;
		}
		return counter;
	}
	
}
