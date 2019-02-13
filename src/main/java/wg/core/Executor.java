package wg.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wg.result.EventResult;
import wg.result.WorkloadResult;
import wg.workload.EventDescriptor;
import wg.workload.Frame;
import wg.workload.Request;
import wg.workload.Target;
import wg.workload.Workload;
import wg.workload.options.FrequencyMode;
import wg.workload.options.FrequencyOption;
import wg.workload.options.RequestsOption;

public class Executor {

	private static final int CORE_MULTIPLICATOR = 2;
	/** Specifies the request execution frequency in milliseconds. */
	private static final int EXECUTION_FREQUENCY = 1;

	private ExecutorService exeService;
	private final ArrayList<EventDescriptor> executedEvents = new ArrayList<EventDescriptor>();
	private static final Logger log = LoggerFactory.getLogger(Executor.class);

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
	 * @throws WorkloadExecutionException
	 */
	public WorkloadResult executeWorkload(Workload workload)
			throws WorkloadExecutionException {
		WorkloadResult result = new WorkloadResult();
		Frame[] frames = workload.getSchedule().getFrames();

		for (int i = 0; i < frames.length; i++) {
			ArrayList<Response> responses = executeFrame(frames[i]);
			for (int j = 0; j < responses.size(); j++) {
				EventDescriptor event = executedEvents.get(j);
				Response response = responses.get(j);
				EventResult eventResult = new EventResult(event, response, j);
				result.addResponse(frames[i], eventResult);
			}
		}

		exeService.shutdown();

		return result;
	}

	private ArrayList<Response> executeFrame(Frame frame)
			throws WorkloadExecutionException {

		ArrayList<Future<Response[]>> futures = new ArrayList<Future<Response[]>>();
		ArrayList<EventDescriptor> events = new ArrayList<EventDescriptor>(
				Arrays.asList(frame.getEvents()));

		for (int s = 0; s < frame.getOptions().getIterations(); s++) {

			long startTime = System.currentTimeMillis();

			EventDescriptor[] sortedEvents = frame.getEvents();
			Arrays.sort(sortedEvents);

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
						log.error(ignore.getMessage());
					}
				}

				int repetitions = getRepetitions(
						frame.getOptions().getRequestsOption(), s);
				for (int r = 0; r < repetitions; r++) {
					long dif = System.currentTimeMillis() - startTime;
					log.error("Event: " + nextEvent.getEventID()
							+ " ausgef�hrt um: " + dif);

//					Future<Response[]> response = executeEvent(nextEvent);
//					futures.add(response);
				}
				index++;
			}
		}

		return parseResponses(futures);
	}

	private Future<Response[]> executeEvent(EventDescriptor currentEvent) {

		Request request = currentEvent.getRequest();
		Target[] targets = currentEvent.getTargets();

		executedEvents.add(currentEvent);
		Future<Response[]> response = exeService
				.submit(new Event(targets, request));

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
	 * @throws WorkloadExecutionException
	 */
	private ArrayList<Response> parseResponses(ArrayList<Future<Response[]>> futures)
			throws WorkloadExecutionException {
		ArrayList<Response> responses = new ArrayList<Response>();
		for (int i = 0; i < futures.size(); i++) {
			Response[] eventResponses = null;
			try {
				eventResponses = (Response[]) futures.get(i).get();
			} catch (ExecutionException | InterruptedException e) {
				futures.get(i).cancel(true);
				exeService.shutdownNow();
				throw new WorkloadExecutionException(
						"Error while executing task!", e);
			}
			for (int j = 0; j < eventResponses.length; j++) {
				responses.add(eventResponses[i]);
			}
		}
		return responses;
	}

}
