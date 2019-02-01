package wg.workload.parser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import wg.requests.BftsmartRequest;
import wg.requests.FtpMethodType;
import wg.requests.FtpRequest;
import wg.requests.HttpMethodType;
import wg.requests.HttpRequest;
import wg.requests.TcpUdpRequest;
import wg.workload.EventDescriptor;
import wg.workload.Frame;
import wg.workload.GrowthType;
import wg.workload.Options;
import wg.workload.ProtocolType;
import wg.workload.Request;
import wg.workload.Schedule;
import wg.workload.Target;
import wg.workload.TransmissionType;
import wg.workload.Workload;

public class WorkloadParser {

	public Workload parseWorkload(String path) throws WorkloadParserException {
		Workload workload = null;
		try {
			Object obj = new JSONParser().parse(new FileReader(path));
			JSONObject jsonObject = (JSONObject) obj;
			HashMap<String, Target> targets = parseTargets(jsonObject);
			HashMap<String, Request> requests = parseRequests(jsonObject);
			Schedule schedule = parseSchedule(jsonObject);
			workload = new Workload(targets, requests, schedule);
		} catch (FileNotFoundException cause) {
			throw new WorkloadParserException("File not found!", cause);
		} catch (ParseException cause) {
			throw new WorkloadParserException("Invalid JSON document!", cause);
		} catch (IOException cause) {
			throw new WorkloadParserException("Could not read from file!",
					cause);
		}
		return workload;
	}

	private HashMap<String, Target> parseTargets(JSONObject jo)
			throws WorkloadParserException {
		JSONArray targets = (JSONArray) jo.get("targets");
		if (targets == null || targets.size() == 0) {
			throw new WorkloadParserException(
					"Targets not found in JSON input!");
		}

		HashMap<String, Target> targetMap = new HashMap<String, Target>();
		for (int i = 0; i < targets.size(); i++) {
			JSONObject targetObj = (JSONObject) targets.get(i);

			String id = (String) targetObj.get("id");
			String server = (String) targetObj.get("server");
			long port = -1;
			if (targetObj.get("port") != null) {
				port = (long) targetObj.get("port");
			}

			try {
				Target target = new Target(id, server, port);
				targetMap.put(id, target);
			} catch (IllegalArgumentException e) {
				throw new WorkloadParserException(
						"Error while parsing target: \"" + id + "\"!", e);
			}
		}

		return targetMap;
	}

	private HashMap<String, Request> parseRequests(JSONObject jo)
			throws WorkloadParserException {

		JSONArray requests = (JSONArray) jo.get("requests");
		if (requests == null || requests.size() == 0) {
			throw new WorkloadParserException(
					"Requests not found in JSON input!");
		}

		HashMap<String, Request> requestMap = new HashMap<String, Request>();
		for (int i = 0; i < requests.size(); i++) {
			JSONObject requestObj = (JSONObject) requests.get(i);
			String id = (String) requestObj.get("id");

			try {
				Request newRequest = getSpecificRequest(requestObj, id);
				requestMap.put(id, newRequest);
			} catch (IllegalArgumentException e) {
				String s = "Error while parsing requests!";
				if (id != null) {
					s = "Error while parsing request: \"" + id + "\"!";
				}
				throw new WorkloadParserException(s, e);
			}
		}

		return requestMap;
	}

	private Schedule parseSchedule(JSONObject jo)
			throws WorkloadParserException {
		JSONObject scheduleObj = (JSONObject) jo.get("schedule");
		if (scheduleObj == null) {
			throw new WorkloadParserException(
					"Schedule not found in JSON input!");
		}

		JSONArray framesObj = (JSONArray) scheduleObj.get("frames");
		if (framesObj == null || framesObj.size() == 0) {
			throw new WorkloadParserException("No frames found in JSON input!");
		}

		Frame[] frames = new Frame[framesObj.size()];
		try {
			for (int i = 0; i < framesObj.size(); i++) {
				JSONObject frameObj = (JSONObject) framesObj.get(i);
				Frame frame = parseFrame(frameObj, i);
				frames[i] = frame;
			}

			return new Schedule(frames);
		} catch (IllegalArgumentException e) {
			throw new WorkloadParserException("Error while parsing schedule!",
					e);
		}
	}

	private Frame parseFrame(JSONObject frameObj, int nameIndex)
			throws WorkloadParserException {
		String id = (String) frameObj.get("id");

		JSONArray eventsObj = (JSONArray) frameObj.get("events");
		EventDescriptor[] events = parseFrameEvents(eventsObj);

		JSONObject optionsObj = (JSONObject) frameObj.get("options");
		Options options = parseFrameOptions(optionsObj);

		Frame frame = new Frame(id, events, options);
		return frame;
	}

	private EventDescriptor[] parseFrameEvents(JSONArray eventsObj)
			throws WorkloadParserException {

		if (eventsObj == null || eventsObj.size() == 0) {
			throw new IllegalArgumentException(
					"No events found in JSON input!");
		}

		EventDescriptor[] events = new EventDescriptor[eventsObj.size()];
		for (int j = 0; j < eventsObj.size(); j++) {
			JSONObject eventObj = (JSONObject) eventsObj.get(j);

			String id = (String) eventObj.get("id");
			String targetName = (String) eventObj.get("target");
			String requestName = (String) eventObj.get("request");

			long time = -1;
			if (eventObj.get("time") != null) {
				time = (long) eventObj.get("time");
			}

			events[j] = new EventDescriptor(id, time, targetName, requestName);
		}

		return events;
	}

	private Options parseFrameOptions(JSONObject optionsObj) {
		// TODO Überarbeiten: Optionen sollen nicht in einzelnes großes Objekt
		// gesteckt werden sondern in mehrere kleine aufgeteilt werden
		if (optionsObj == null) {
			Options options = new Options(-1, -1, GrowthType.NONE, -1, -1,
					false, false, TransmissionType.NONE);
			return options;
		}

		// RepeatEventsOption
		long eventNumberSteps;
		long eventLinearGrowthFactor;
		GrowthType eventGrowthType;
		JSONObject repeatEventsObj = (JSONObject) optionsObj
				.get("repeatEvents");
		if (repeatEventsObj == null) {
			eventNumberSteps = -1;
			eventLinearGrowthFactor = -1;
			eventGrowthType = GrowthType.NONE;
		} else {
			if (repeatEventsObj.get("steps") == null) {
				eventNumberSteps = -1;
			} else {
				eventNumberSteps = (Long) repeatEventsObj.get("steps");
			}
			if (repeatEventsObj.get("linearGrowthFactor") == null) {
				eventLinearGrowthFactor = -1;
			} else {
				eventLinearGrowthFactor = (Long) repeatEventsObj
						.get("linearGrowthFactor");
			}
			if (repeatEventsObj.get("growth") == null) {
				eventGrowthType = GrowthType.NONE;
			} else {
				String growth = (String) repeatEventsObj.get("growth");
				eventGrowthType = GrowthType.parseString(growth);
			}
		}
		// FrequencyOption
		long frequencySteps = -1;
		long frequencyFactor = -1;
		boolean frequencyIncrease = false;
		boolean frequencyDecrease = false;
		JSONObject increaseFrequencyObj = (JSONObject) optionsObj
				.get("increaseFrequency");
		JSONObject decreaseFrequencyObj = (JSONObject) optionsObj
				.get("decreaseFrequency");
		if (increaseFrequencyObj != null) {
			frequencyIncrease = true;
			if (increaseFrequencyObj.get("steps") == null) {
				frequencySteps = -1;
			} else {
				frequencySteps = (Long) increaseFrequencyObj.get("steps");
			}
			if (increaseFrequencyObj.get("factor") == null) {
				frequencyFactor = -1;
			} else {
				frequencyFactor = (Long) increaseFrequencyObj.get("factor");
			}
		}
		if (decreaseFrequencyObj != null) {
			frequencyDecrease = true;
			if (decreaseFrequencyObj.get("steps") == null) {
				frequencySteps = -1;
			} else {
				frequencySteps = (Long) decreaseFrequencyObj.get("steps");
			}
			if (decreaseFrequencyObj.get("factor") == null) {
				frequencyFactor = -1;
			} else {
				frequencyFactor = (Long) decreaseFrequencyObj.get("factor");
			}
		}
		// TransmissionOption
		String transmissionName = (String) optionsObj.get("transmission");
		TransmissionType transmissionType;
		if (transmissionName == null) {
			transmissionType = TransmissionType.parseString(transmissionName);
		} else {
			transmissionType = TransmissionType.NONE;
		}
		Options options = new Options(eventNumberSteps, eventLinearGrowthFactor,
				eventGrowthType, frequencySteps, frequencyFactor,
				frequencyIncrease, frequencyDecrease, transmissionType);
		return options;
	}

	private Request getSpecificRequest(JSONObject requestContent,
			String requestName) throws WorkloadParserException {

		String protocol = (String) requestContent.get("protocol");
		if (protocol == null) {
			throw new IllegalArgumentException(
					"Protocol not found in JSON input!");
		}

		ProtocolType protocolType = ProtocolType.fromString(protocol);
		switch (protocolType) {

		case HTTP:
			return createHttpRequest(requestContent, requestName, protocolType);
		case FTP:
			return createFtpRequest(requestContent, requestName, protocolType);
		case TCP:
			return createTcpUdpRequest(requestContent, requestName,
					protocolType);
		case UDP:
			return createTcpUdpRequest(requestContent, requestName,
					protocolType);
		case BFTSMaRt:
			return createBftsmartRequest(requestContent, requestName,
					protocolType);
		default:
			return null;
		}

	}

	private Request createHttpRequest(JSONObject requestContent,
			String requestName, ProtocolType protocolType)
			throws WorkloadParserException {

		String resourcePath = (String) requestContent.get("resourcePath");
		String content = (String) requestContent.get("content");

		String methodTypeName = (String) requestContent.get("method");
		if (methodTypeName == null) {
			throw new IllegalArgumentException(
					"Method not found in JSON input!");
		}
		HttpMethodType methodType = HttpMethodType.fromString(methodTypeName);

		return new HttpRequest(requestName, protocolType, methodType,
				resourcePath, content);
	}

	private Request createFtpRequest(JSONObject requestContent,
			String requestName, ProtocolType protocolType)
			throws WorkloadParserException {

		String localResource = (String) requestContent.get("localResource");
		String remoteResource = (String) requestContent.get("remoteResource");
		String username = (String) requestContent.get("username");
		String password = (String) requestContent.get("password");

		String methodTypeName = (String) requestContent.get("method");
		if (methodTypeName == null) {
			throw new IllegalArgumentException(
					"Method not found in JSON input!");
		}
		FtpMethodType methodType = FtpMethodType.fromString(methodTypeName);

		return new FtpRequest(requestName, protocolType, methodType,
				localResource, remoteResource, username, password);
	}

	private Request createTcpUdpRequest(JSONObject requestContent,
			String requestName, ProtocolType protocolType)
			throws WorkloadParserException {

		String content = (String) requestContent.get("content");

		return new TcpUdpRequest(requestName, protocolType, content);
	}

	private Request createBftsmartRequest(JSONObject requestContent,
			String requestName, ProtocolType protocolType)
			throws WorkloadParserException {

		String command = (String) requestContent.get("command");
		String type = (String) requestContent.get("type");

		JSONArray targetGroupObj = (JSONArray) requestContent
				.get("targetGroup");
		if (targetGroupObj == null || targetGroupObj.size() == 0) {
			throw new IllegalArgumentException(
					"No target group found in JSON input!");
		}
		ArrayList<String> targetGroup = new ArrayList<String>();
		for (int i = 0; i < targetGroupObj.size(); i++) {
			targetGroup.add((String) targetGroupObj.get(i));
		}

		return new BftsmartRequest(requestName, protocolType, command, type,
				targetGroup);
	}

}
