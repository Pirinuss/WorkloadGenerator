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
import wg.workload.FrameModeType;
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
	 * Parses the defined frame mode to the right execution method
	 * @param frame The frame that gets executed
 	 * @return The array which contains all the responses of the
	 * executed events of the frame
	 */
	private Response[] executeFrame(Frame frame) {
		FrameModeType mode = frame.getFrameMode();
		switch (mode) {
		case DEFINEDTIME:
			return executeFrameWithoutIncrease(frame, false);
		case REPEAT:
			return executeFrameWithoutIncrease(frame, true);
		case INCREASEEXPO:
			return executeFrameWithIncrease(frame, true);
		case INCREASEFIB:
			return executeFrameWithIncrease(frame, false);	
		default:
			return null;
		}
	}
	
	/**
	 * Executes a frame without increase mode: Extracts the events from the frame, calls the execution for
	 * each event by the right time and the right amount of repetitions. Returns the responses afterwards.
	 * @param frame The frame that gets executed
	 * @return responses The array which contains all the responses of the
	 * executed events
	 */
	private Response[] executeFrameWithoutIncrease(Frame frame, boolean withReps) {
		ArrayList<EventDiscriptor> events = new ArrayList<EventDiscriptor>(Arrays.asList(frame.getEvents()));
		EventDiscriptor currentEvent;
		long exeTime;
		long repetitions;
		long size;
		if (withReps) {
			size = getTotalEventsNumber(events, 1);
		} else {
			size = events.size();
		}
		Response[] responses = new Response[(int) size];
		ArrayList<Future<Response>> futures = new ArrayList<Future<Response>>();
		executedEvents = new ArrayList<EventDiscriptor>();
		Timestamp startTime = new Timestamp(System.currentTimeMillis());
		while (events.size() > 0) {
			for (int i=0; i<events.size(); i++) {
				currentEvent = events.get(i);
				if (withReps) {
					repetitions = currentEvent.getRepetitions();
				} else {
					repetitions = 1;
				}
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
			events = removeEvents(events, executedEvents);
		}
		return parseResponses(futures, responses);
	}
	
	/**
	 * Executes a frame with increase mode: Extracts the events from the frame, calls the execution for
	 * each event by the right time and the right amount of repetitions. Repeats it for each steps while increasing 
	 * the amount of repetitions for each event exponential or following the Fibonacci sequence. Returns the 
	 * responses afterwards.
	 * @param frame The frame that gets executed
	 * @return responses The array which contains all the responses of the
	 * executed events
	 */
	private Response[] executeFrameWithIncrease(Frame frame, boolean isExpo) {
		ArrayList<EventDiscriptor> events = new ArrayList<EventDiscriptor>(Arrays.asList(frame.getEvents()));
		EventDiscriptor currentEvent;
		long exeTime;
		long repetitions;
		long steps = frame.getSteps();
		long size;
		if (isExpo) {
			size = getTotalEventsNumber(events, steps);
		} else {
			size = getTotalEventsNumberFib(events, steps);
		}
		Response[] responses = new Response[(int) size];
		ArrayList<Future<Response>> futures = new ArrayList<Future<Response>>();
		executedEvents = new ArrayList<EventDiscriptor>();
		for (int s=1; s<=steps; s++) {
			events = new ArrayList<EventDiscriptor>(Arrays.asList(frame.getEvents()));
			ArrayList<EventDiscriptor> executedEventsPerStep = new ArrayList<EventDiscriptor>();
			Timestamp startTime = new Timestamp(System.currentTimeMillis());
			while (events.size() > 0) {
				for (int i=0; i<events.size(); i++) {
					currentEvent = events.get(i);
					if (isExpo) {
						repetitions = currentEvent.getRepetitions();
						repetitions = (long) Math.pow( repetitions, s);
					} else {
						repetitions = calculateFibNumber(s);
					}
					exeTime = currentEvent.getTime();
					Timestamp currentTime = new Timestamp(System.currentTimeMillis());
					long dif = currentTime.getTime()-startTime.getTime();
					if (currentTime.getTime()-exeTime >=startTime.getTime() ) {
						for (int r=0; r<repetitions; r++) {
							System.out.println("Event: " + currentEvent.getEventName() +  " ausgeführt um: " + dif);
							Future<Response> response = executeEvent(currentEvent);
							executedEventsPerStep.add(currentEvent);
							futures.add(response);
						}
					}
				}
				events = removeEvents(events, executedEventsPerStep);
			}
		}
		return parseResponses(futures, responses);
	}
	
	/**
	 * Calculates the right number of the Fibonacci sequence for the given position
	 * @param position The position of the number in the Fibonacci sequence
	 * @return The right number of the Fibonacci sequence
	 */
	private int calculateFibNumber(long position) {
		if (position<=0) {
			return 0;
		} else if (position == 1) {
			return 1;
		} else {
			return calculateFibNumber(position-2)+calculateFibNumber(position-1);
		}
	}
	
	/**
	 * Removes the executed events from the event array.
	 * @param events The array of all events
	 * @param executedEvents The array of all executed events
	 * @return Returns the array of all events that still have to executed
	 */
	private ArrayList<EventDiscriptor> removeEvents(ArrayList<EventDiscriptor> events, ArrayList<EventDiscriptor> executedEvents) {
		for (int j=0; j<executedEvents.size(); j++) {
			for (int x =0; x<events.size(); x++) {
				if (events.get(x).equals(executedEvents.get(j))) {
					events.remove(events.get(x));
				}
			}
		}
		return events;
	}
	
	/**
	 * Parses the future objects to responses that are added to the response array afterwards.
	 * @param futures The future object which includes the response
	 * @param responses The array of responses
	 * @return The filled array of responses
	 */
	private Response[] parseResponses(ArrayList<Future<Response>> futures, Response[] responses) {
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
	 * Returns the target object for a target name.
	 * @param targetName The namen of the target object
	 * @return target The target object
	 */
	private Target mapTarget(String targetName) {
		HashMap<String, Target> targets = w.getTargets();
		Target target = targets.get(targetName);
		return target;
	}
	
	/**
	 * Returns the request object for a request name.
	 * @param requesttName The namen of the request object
	 * @return request The request object
	 */
	private Request mapRequest(String requestName) {
		HashMap<String, Request> requests = w.getRequests();
		Request request = requests.get(requestName);
		return request;
	}
	
	/**
	 * Returns the number (including repetitions and increase) of events that have to 
	 * be executed.
	 * @param events The list of events from the frame
	 * @return The number of events for a frame
	 */
	private long getTotalEventsNumber(ArrayList<EventDiscriptor> events, long steps) {
		long counter = 0;
		long[] eventReps = new long[events.size()];
		for (int i=0; i<events.size(); i++) {
			long reps = events.get(i).getRepetitions();
			eventReps[i] = reps;
			counter = counter + reps;
		}
		for (int j=1; j<steps; j++) {
			for (int k=0; k<eventReps.length; k++) {
				long addValue = (long) Math.pow(eventReps[k], 2);
				eventReps[k] = addValue;
				counter = counter + addValue;
			}
		}
		return counter;
	}
	
	private long getTotalEventsNumberFib(ArrayList<EventDiscriptor> events, long steps) {
		long counter = 0;
		for (int i=0; i<steps; i++) {
			long addValue = calculateFibNumber(i+1) * events.size();
			counter = counter + addValue;
		}
		return counter;
	}
	
}
