package wg.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import wg.core.Response;
import wg.responses.HttpResponseObject;
import wg.responses.TcpUdpResponse;
import wg.workload.EventDescriptor;
import wg.workload.Frame;
import wg.workload.GrowthType;

public class WorkloadResult {

	private final HashMap<Frame, ArrayList<EventResult>> results = new HashMap<Frame, ArrayList<EventResult>>();

	public void addResponse(Frame frame, EventResult resultObject) {
		if (!results.containsKey(frame)) {
			ArrayList<EventResult> frameResultList = new ArrayList<EventResult>();
			frameResultList.add(resultObject);
			results.put(frame, frameResultList);
		} else {
			results.get(frame).add(resultObject);
		}
	}

	public void printResponses() {
		for (Entry<Frame, ArrayList<EventResult>> entry : results.entrySet()) {
			Frame frame = entry.getKey();
			ArrayList<EventResult> frameResults = entry.getValue();
			printFrameInfos(frame, frameResults);
			for (int i = 0; i < frameResults.size(); i++) {
				EventDescriptor event = frameResults.get(i).getEvent();
				Response response = frameResults.get(i).getResponse();
				printResponseInfos(event, response);
			}
		}
	}

	private void printFrameInfos(Frame frame, ArrayList<EventResult> value) {
		// @formatter:off
		System.out.println(
				"---Ergebnisse f�r Frame " + frame.getFrameID() + " ---");
		System.out.println(" Definierte Events: " + frame.getEvents().length);
		System.out.println(" Definierte Optionen:");
		System.out.println("   Übertragung: " + frame.getOptions().getTransmissionType());
		System.out.print("   Eventanzahl: ");
		System.out.print("Wachstum: " + frame.getOptions().getRequestsOption().getGrowthType());
		if (frame.getOptions().getRequestsOption().getGrowthType() == GrowthType.LINEAR) {
			System.out.print(" , Wachstumsfaktor: " + frame.getOptions().getRequestsOption().getLinearGrowthFactor());
		}
		System.out.println(" , Wiederholungen: " + frame.getOptions().getIterations());
		if (frame.getOptions().getFrequencyOption().getFactor() != 1) {
			System.out.print("   Frequenzänderung: ");
			System.out.print("Modus: " + frame.getOptions().getFrequencyOption().getMode());
			System.out.print(" , Änderungsfaktor: " + frame.getOptions().getFrequencyOption().getFactor());
			System.out.println(" , Wiederholungen: " + frame.getOptions().getIterations());
		}
		System.out.println(" Insgesamt ausgef�hrte Events: " + value.size());
		System.out.println();
		// @formatter:on
	}

	private void printResponseInfos(EventDescriptor event, Response response) {
		// @formatter:off
		System.out.println("   Event: " + event.getEventID());
		if (response == null) {
			System.out.println("     Failed");
			return;
		}

		switch (event.getRequest().getProtocol()) {
		case BFTSMaRt:
			System.out.println("     Anzahl Targets: " + response.getTargetGroup().length);
			System.out.println("     Execution time: " + (response.getStopTime() - response.getStartTime()));
			//BftsmartResponse bftsmartResponse = (BftsmartResponse) response;
			break;
		case FTP:
			System.out.println("     Target: " + response.getTarget().getServerName());
			System.out.println("     Execution time: " + (response.getStopTime() - response.getStartTime()));
			break;
		case HTTP:
			System.out.println("     Target: " + response.getTarget().getServerName());
			System.out.println("     Execution time: " + (response.getStopTime() - response.getStartTime()));
			HttpResponseObject httpResponse = (HttpResponseObject) response;
			System.out.println("     Response Code: " + httpResponse.getResponse().getStatusLine());
			System.out.println("     Response L�nge: " + httpResponse.getResponse().getEntity().getContentLength());
			break;
		case TCP:
			System.out.println("     Target: " + response.getTarget().getServerName());
			System.out.println("     Execution time: " + (response.getStopTime() - response.getStartTime()));
			TcpUdpResponse tcpResponse = (TcpUdpResponse) response;
			System.out.println("     Response: " + tcpResponse.getContent());
			break;
		case UDP:
			System.out.println("     Target: " + response.getTarget().getServerName());
			System.out.println("     Execution time: " + (response.getStopTime() - response.getStartTime()));
			TcpUdpResponse udpResponse = (TcpUdpResponse) response;
			System.out.println("     Response: " + udpResponse.getContent());
			break;
		}
		// @formatter: on

	}

}
