package wg.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import wg.requests.BftsmartRequest;
import wg.workload.EventDescriptor;
import wg.workload.Frame;
import wg.workload.ProtocolType;
import wg.workload.Request;
import wg.workload.Target;
import wg.workload.Workload;
import wg.workload.options.Clients;
import wg.workload.options.FrequencyMode;
import wg.workload.options.FrequencyOption;
import wg.workload.options.Options;
import wg.workload.options.RequestsOption;

public class Executor {

	private static final int CORE_MULTIPLICATOR = 2;
	/** Specifies the request execution frequency in milliseconds. */
	private static final int EXECUTION_FREQUENCY = 1;

	private ExecutorService exeService;
	private final ArrayList<EventDescriptor> executedEvents = new ArrayList<EventDescriptor>();
	private static final Logger log = Logger.getLogger("logfile.txt");

	public Executor() {
		int threadPoolSize = Runtime.getRuntime().availableProcessors()
				* CORE_MULTIPLICATOR;
		exeService = Executors.newFixedThreadPool(threadPoolSize);
	}

	/**
	 * Executes the workload: Extracts the frames of the workload, calls the
	 * execution for each one and returns the result
	 * 
	 * @param workload
	 *            The workload that gets executed
	 * @return result The results of the workloads execution
	 */
	public Result executeWorkload(Workload workload) {
		Result result = new Result();
		Frame[] frames = workload.getSchedule().getFrames();

		for (int i = 0; i < frames.length; i++) {
			Response[] responses = executeFrame(frames[i]);
			for (int j = 0; j < responses.length; j++) {
				EventDescriptor event = executedEvents.get(j);
				Response response = responses[j];
				ResultObject resultObject = new ResultObject(frames[i], event,
						response, j);
				result.safeResponse(resultObject);
			}
		}

		exeService.shutdown();

		return result;
	}

	private Response[] executeFrame(Frame frame) {
		ArrayList<Future<Response>> futures = new ArrayList<Future<Response>>();
		ArrayList<EventDescriptor> events = new ArrayList<EventDescriptor>(
				Arrays.asList(frame.getEvents()));

		long steps = getMaximalSteps(frame.getOptions());

		for (int s = 0; s <= steps; s++) {
			events = new ArrayList<EventDescriptor>(
					Arrays.asList(frame.getEvents()));

			long startTime = System.currentTimeMillis();

			EventDescriptor[] sortedEvents = frame.getEvents();
			Arrays.sort(sortedEvents);

			if (sortedEvents.length < 1) {
				return new Response[0];
			}

			int index = 0;

			while (index < events.size()) {
				EventDescriptor nextEvent = sortedEvents[index];

				long eventExecution = startTime + calculateExeTime(
						frame.getOptions().getFrequencyOption(),
						nextEvent.getTime(), s);
				while (eventExecution > System.currentTimeMillis()) {
					try {
						Thread.sleep(EXECUTION_FREQUENCY);
					} catch (InterruptedException ignore) {
						log.warning(ignore.getMessage());
					}
				}

				int clientsNumber = frame.getOptions().getClients()
						.getClientsNumber(nextEvent.getRequest().getProtocol());
				frame.getOptions().getClients();
				for (int i = 0; i < clientsNumber; i++) {

					int repetitions = getRepetitions(
							frame.getOptions().getRequestsOption(), s);
					for (int r = 0; r < repetitions; r++) {
						long dif = System.currentTimeMillis() - startTime;
						log.info("Event: " + nextEvent.getEventID()
								+ " ausgeführt durch Client " + i + " um: "
								+ dif);

						// Future<Response> response = executeEvent(nextEvent,
						// frame.getOptions().getClients(), i);
						// futures.add(response);
					}
				}
				index++;
			}
		}

		return parseResponses(futures);
	}

	private Future<Response> executeEvent(EventDescriptor currentEvent,
			Clients clients, int clientIndex) {

		Event event;
		Request request = currentEvent.getRequest();

		if (request.getProtocol() == ProtocolType.BFTSMaRt) {
			BftsmartRequest bftRequest = (BftsmartRequest) request;
			Target[] targetGroup = bftRequest.getTargetGroup();
			event = new Event(targetGroup, request, clients, clientIndex);
		} else {
			Target target = currentEvent.getTarget();
			event = new Event(target, request, clients, clientIndex);
		}

		Future<Response> response = exeService.submit(event);

		return response;
	}

	private long calculateExeTime(FrequencyOption option, long initExeTime,
			int currentStep) {
		if (currentStep == 0) {
			return initExeTime;
		}
		long exeTime = initExeTime;
		long factor = option.getFactor();
		if (option.getMode() == FrequencyMode.INCREASE) {
			for (int i = 0; i < currentStep; i++) {
				exeTime = exeTime / factor;
			}
		}
		if (option.getMode() == FrequencyMode.DECREASE) {
			for (int i = 0; i < currentStep; i++) {
				exeTime = exeTime * factor;
			}
		}
		return exeTime;
	}

	private int getRepetitions(RequestsOption option, int currentStep) {
		if (currentStep == 0) {
			return 1;
		}
		int repetitions = 0;
		switch (option.getGrowthType()) {
		case INCREASEEXPO:
			repetitions = (int) Math.pow(2, currentStep);
			break;
		case INCREASEFIB:
			repetitions = calculateFibRepetitions(currentStep + 1);
			break;
		case LINEAR:
			repetitions = (int) ((currentStep)
					* option.getLinearGrowthFactor());
			break;
		}
		return repetitions;
	}

	/**
	 * Calculates the right number of the Fibonacci sequence for the given
	 * position
	 * 
	 * @param position
	 *            The position of the number in the Fibonacci sequence
	 * @return The right number of the Fibonacci sequence
	 */
	private int calculateFibRepetitions(long position) {
		if (position <= 0) {
			return 0;
		} else if (position == 1) {
			return 1;
		} else {
			return calculateFibRepetitions(position - 2)
					+ calculateFibRepetitions(position - 1);
		}
	}

	/**
	 * Parses the future objects to responses that are added to the response
	 * array afterwards.
	 * 
	 * @param futures
	 *            The future object which includes the response
	 * @param responses
	 *            The array of responses
	 * @return The filled array of responses
	 */
	private Response[] parseResponses(ArrayList<Future<Response>> futures) {
		Response[] responses = new Response[futures.size()];
		for (int i = 0; i < futures.size(); i++) {
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
		long repeatEventsSteps = options.getRequestsOption().getSteps();
		long changeFrequencySteps = options.getFrequencyOption().getSteps();
		if (repeatEventsSteps == -1 && changeFrequencySteps == -1) {
			return 1;
		}
		if (repeatEventsSteps >= changeFrequencySteps) {
			return repeatEventsSteps;
		}
		return changeFrequencySteps;
	}

}
