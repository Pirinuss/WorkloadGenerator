package wg.core;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import wg.workload.EventDiscriptor;
import wg.workload.Frame;
import wg.workload.Options;
import wg.workload.Request;
import wg.workload.Target;
import wg.workload.Workload;

public class Executor {
	
	private ExecutorService exeService;
	private Workload workload;
	private final ArrayList<EventDiscriptor> executedEvents = new ArrayList<EventDiscriptor>();
	private static final Logger log = Logger.getLogger("logfile.txt");


	/**
	 * Executes the workload: Extracts the frames of the workload, calls the execution
	 * for each one and returns the result 
	 * @param workload The workload that gets executed
	 * @return result The results of the workloads execution
	 */
	public Result executeWorkload(Workload workload) {
		this.workload = workload;
		Result result = new Result();
		Frame[] frames = workload.getSchedule().getFrames();
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
	 * Executes a frame with increase mode: Extracts the events from the frame, calls the execution for
	 * each event by the right time and the right amount of repetitions. Repeats it for each steps while increasing 
	 * the amount of repetitions for each event exponential or following the Fibonacci sequence. Returns the 
	 * responses afterwards.
	 * @param frame The frame that gets executed
	 * @return responses The array which contains all the responses of the
	 * executed events
	 */
	private Response[] executeFrame(Frame frame) {
		ArrayList<Future<Response>> futures = new ArrayList<Future<Response>>();
		ArrayList<EventDiscriptor> events = new ArrayList<EventDiscriptor>(Arrays.asList(frame.getEvents()));
		long steps = getMaximalSteps(frame.getOptions());
		for (int s=1; s<=steps; s++) {
			events = new ArrayList<EventDiscriptor>(Arrays.asList(frame.getEvents()));
			ArrayList<EventDiscriptor> executedEventsPerStep = new ArrayList<EventDiscriptor>();
			Timestamp startTime = new Timestamp(System.currentTimeMillis());
			while (events.size() > 0) {
				for (int i=0; i<events.size(); i++) {
					EventDiscriptor currentEvent = events.get(i);
					long exeTime = calculateExeTime(frame.getOptions(), currentEvent.getTime(), s);
					Timestamp currentTime = new Timestamp(System.currentTimeMillis());
					long dif = currentTime.getTime()-startTime.getTime();
					if (currentTime.getTime()-exeTime >=startTime.getTime() ) {
						int repetitions = getRepetitions(frame.getOptions(), s);
						for (int r=0; r<repetitions; r++) {
							log.info("Event: " + currentEvent.getEventName() +  " ausgeführt um: " + dif);
							//Future<Response> response = executeEvent(currentEvent);
							executedEventsPerStep.add(currentEvent);
							//futures.add(response);
						}
					}
				}
				events = removeEvents(events, executedEventsPerStep);
			}
		}
		return parseResponses(futures);
	}
	
	private long calculateExeTime(Options options, long initExeTime, int currentStep) {
		long exeTime = initExeTime;
		long factor = options.getFrequencyFactor();
		if (options.isFrequencyIncrease()) {
			for (int i=0; i<currentStep; i++) {
				exeTime = exeTime/factor;
			}
		}
		if (options.isFrequencyDecrease()) {
			for (int i=0; i<currentStep; i++) {
				exeTime = exeTime*factor;
			}
		}
		return exeTime;
	}
	
	private int getRepetitions(Options options, int currentStep) {
		int repetitions = 0;
		switch (options.getEventGrowthType()) {
		case INCREASEEXPO:
			repetitions = (int) Math.pow(2, currentStep);
			break;
		case INCREASEFIB:
			repetitions = calculateFibRepetitions(currentStep);
			break;
		case LINEAR:
			repetitions = (int) ((currentStep) * options.getEventLinearGrowthFactor());
			break;
		case NONE:
			repetitions = 1;
			break;
		}
		return repetitions;
	}
	
	/**
	 * Calculates the right number of the Fibonacci sequence for the given position
	 * @param position The position of the number in the Fibonacci sequence
	 * @return The right number of the Fibonacci sequence
	 */
	private int calculateFibRepetitions(long position) {
		if (position<=0) {
			return 0;
		} else if (position == 1) {
			return 1;
		} else {
			return calculateFibRepetitions(position-2)+calculateFibRepetitions(position-1);
		}
	}
	
	/**
	 * Removes the executed events from the event array.
	 * @param events The array of all events
	 * @param executedEvents The array of all executed events
	 * @return Returns the array of all events that still have to executed
	 */
	private ArrayList<EventDiscriptor> removeEvents(ArrayList<EventDiscriptor> events,
			ArrayList<EventDiscriptor> executedEvents) {
		for (int j = 0; j < executedEvents.size(); j++) {
			for (int x = 0; x < events.size(); x++) {
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
	private Response[] parseResponses(ArrayList<Future<Response>> futures) {
		Response[] responses = new Response[futures.size()];
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
	
	private long getMaximalSteps(Options options) {
		long repeatEventsSteps = options.getEventNumberSteps();
		long changeFrequencySteps = options.getFrequencySteps();
		if (repeatEventsSteps == -1 && changeFrequencySteps == -1) {
			return 1;
		}
		if (repeatEventsSteps >= changeFrequencySteps) {
			return repeatEventsSteps;
		} 
		return changeFrequencySteps;
	}
	
	/**
	 * Maps the event discription to an event object and executes it.
	 * Returns the response as a future object.
	 * @param currentEvent The event that gets mapped
	 * @return response The response of the event as a future object
	 */
	private Future<Response> executeEvent(EventDiscriptor currentEvent) {
		Target target;
		if (currentEvent.getTargetName() == null) {
			target = null;
		} else {
			target = mapTarget(currentEvent.getTargetName());
		}
		Request request = mapRequest(currentEvent.getRequestName());
		Event event = new Event(target, request, workload);
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
		HashMap<String, Target> targets = workload.getTargets();
		Target target = targets.get(targetName);
		return target;
	}
	
	/**
	 * Returns the request object for a request name.
	 * @param requesttName The namen of the request object
	 * @return request The request object
	 */
	private Request mapRequest(String requestName) {
		HashMap<String, Request> requests = workload.getRequests();
		Request request = requests.get(requestName);
		return request;
	}
	
}
