package wg.Execution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import wg.responses.Response;
import wg.workload.Frame;
import wg.workload.GrowthType;

public class WorkloadResult {

	private final HashMap<Frame, ArrayList<Response>> results = new HashMap<Frame, ArrayList<Response>>();
	private final HashMap<Frame, Long> startTimes = new HashMap<Frame, Long>();
	private final HashMap<Frame, Long> endTimes = new HashMap<Frame, Long>();

	public void addResponse(Frame frame, Response response) {
		if (!results.containsKey(frame)) {
			ArrayList<Response> frameResultList = new ArrayList<Response>();
			frameResultList.add(response);
			results.put(frame, frameResultList);
		} else {
			results.get(frame).add(response);
		}
	}

	public void addTimes(Frame frame, long startTime, long endTime) {
		startTimes.put(frame, startTime);
		endTimes.put(frame, endTime);
	}

	public void printResponses(boolean printInDetail) {
		for (Entry<Frame, ArrayList<Response>> entry : results.entrySet()) {
			Frame frame = entry.getKey();
			ArrayList<Response> frameResults = entry.getValue();
			System.out.println();
			printFrameInfos(frame, frameResults);
			if (printInDetail) {
				System.out.println();
				System.out.println(" --- Results per request ---");
				for (int i = 0; i < frameResults.size(); i++) {
					Response response = frameResults.get(i);
					System.out.println("   Request: " + (i+1));
					if (response == null) {
						System.out.println("     Failed");
					} else {
						response.print();
					}
				}
			}
		}
	}

	private void printFrameInfos(Frame frame, ArrayList<Response> value) {

		double exeTime = (double) (endTimes.get(frame) - (double) startTimes.get(frame)) / 1000.0;
		double averageRttTime = calculateAvgTime(frame, value);

		// @formatter:off
		System.out.println(
				"-----Result for frame: " + frame.getFrameID() + " -----");
		System.out.println();
		System.out.println(" --- Input ---");
		System.out.println(" Defined events: " + frame.getEvents().length);
		System.out.println(" Defined options:");
		System.out.println("   Transmission: " + frame.getOptions().getTransmissionType());
		System.out.println("   Iterations: " + frame.getOptions().getIterations());
		if (frame.getOptions().getIterations() > 1) {
			System.out.print("   Request number: ");
			System.out.print("Growth: " + frame.getOptions().getRequestsOption().getGrowthType());
			if (frame.getOptions().getRequestsOption().getGrowthType() == GrowthType.LINEAR) {
				System.out.print(" , Growth factor: " + frame.getOptions().getRequestsOption().getLinearGrowthFactor());
			}
			System.out.println();
			if (frame.getOptions().getFrequencyOption().getFactor() != 1) {
				System.out.print("   Frequency: ");
				System.out.print("Mode: " + frame.getOptions().getFrequencyOption().getMode());
				System.out.println(" , Factor: " + frame.getOptions().getFrequencyOption().getFactor());
			}
		}
		System.out.println();
		System.out.println(" --- Results --- ");
		System.out.println("   Total execution time for frame (in seconds): " + exeTime);
		System.out.println("   Total amount of executed requests: " + value.size());
		System.out.println("   Average RTT (in milliseconds): " + averageRttTime);
		// @formatter:on
	}

	private double calculateAvgTime(Frame frame, ArrayList<Response> value) {
		int totalEvents = value.size();
		int totalTime = 0;
		for (Response response : value) {
			totalTime += response.getRTT();
		}
		return ((double) totalTime / (double) totalEvents);
	}

}
