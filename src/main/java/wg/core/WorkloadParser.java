package wg.core;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

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
import wg.workload.EventDiscriptor;
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

	private static final Logger log = Logger.getLogger("logfile.txt");

	public Workload parseWorkload(String path) {
		Workload workload = null;
		try {
			Object obj = new JSONParser().parse(new FileReader(path));
			JSONObject jsonObject = (JSONObject) obj;
			HashMap<String, Target> targets = parseTargets(jsonObject);
			HashMap<String, Request> requests = parseRequests(jsonObject);
			Schedule schedule = parseSchedule(jsonObject);
			workload = new Workload(targets, requests, schedule);
		} catch (FileNotFoundException e) {
			log.severe("File not found");
		} catch (ParseException e) {
			log.severe("Invalid JSON document");
		} catch (IllegalArgumentException e) {
			log.severe(e.getMessage());
		} catch (Exception e) {
			log.severe(e.getMessage());
			e.printStackTrace();
		}
		log.fine("Finished with parsing workload");
		return workload;
	}

	private HashMap<String, Target> parseTargets(JSONObject jo) {
		JSONArray targets = (JSONArray) jo.get("targets");
		if (targets == null) {
			return null;
		}
		HashMap<String, Target> targetMap = new HashMap<String, Target>();
		for (int i = 0; i < targets.size(); i++) {
			JSONObject targetObj = (JSONObject) targets.get(i);
			String targetName = ("target").concat(String.valueOf(i + 1));
			JSONObject targetContent = (JSONObject) targetObj.get(targetName);
			if (targetContent == null) {
				targetMap.put(targetName, null);
			} else {
				String servername = (String) targetContent.get("servername");
				String port = (String) targetContent.get("port");
				Target newTarget = new Target(targetName, servername, port);
				targetMap.put(targetName, newTarget);
			}
		}
		return targetMap;

	}

	private HashMap<String, Request> parseRequests(JSONObject jo) {
		JSONArray requests = (JSONArray) jo.get("requests");
		if (requests == null) {
			return null;
		}
		HashMap<String, Request> requestMap = new HashMap<String, Request>();
		for (int i = 0; i < requests.size(); i++) {
			JSONObject requestObj = (JSONObject) requests.get(i);
			String requestName = ("request").concat(String.valueOf(i + 1));
			JSONObject requestContent = (JSONObject) requestObj.get(requestName);
			if (requestContent == null) {
				requestMap.put(requestName, null);
			} else {
				Request newRequest = getSpecificRequest(requestContent, requestName);
				requestMap.put(requestName, newRequest);
			}
		}
		return requestMap;
	}

	private Schedule parseSchedule(JSONObject jo) {
		JSONObject scheduleObj = (JSONObject) jo.get("schedule");
		if (scheduleObj == null) {
			return null;
		}
		JSONArray framesObj = (JSONArray) scheduleObj.get("frames");
		Frame[] frames;
		if (framesObj == null) {
			frames = null;
		} else {
			frames = new Frame[framesObj.size()];
			for (int i = 0; i < framesObj.size(); i++) {
				JSONObject frameObj = (JSONObject) framesObj.get(i);
				Frame frame = parseFrame(frameObj, i);
				frames[i] = frame;
			}
		}
		Schedule schedule = new Schedule(frames);
		return schedule;
	}

	private Frame parseFrame(JSONObject frameObj, int nameIndex) {
		String frameName = ("frame").concat(String.valueOf(nameIndex + 1));
		JSONObject frameContent = (JSONObject) frameObj.get(frameName);
		if (frameContent == null) {
			return null;
		}
		JSONArray eventsObj = (JSONArray) frameContent.get("events");
		EventDiscriptor[] events = parseFrameEvents(eventsObj);
		JSONObject optionsObj = (JSONObject) frameContent.get("options");
		Options options = parseFrameOptions(optionsObj);
		Frame frame = new Frame(frameName, events, options);
		return frame;
	}

	private EventDiscriptor[] parseFrameEvents(JSONArray eventsObj) {
		if (eventsObj == null) {
			return null;
		}
		EventDiscriptor[] events = new EventDiscriptor[eventsObj.size()];
		for (int j = 0; j < eventsObj.size(); j++) {
			JSONObject eventObj = (JSONObject) eventsObj.get(j);
			String eventName = ("event").concat(String.valueOf(j + 1));
			JSONObject eventContent = (JSONObject) eventObj.get(eventName);
			if (eventContent == null) {
				events[j] = null;
				break;
			}
			String targetName = (String) eventContent.get("target");
			String requestName = (String) eventContent.get("request");
			long time = -1;
			if (eventContent.get("time") != null) {
				time = (Long) eventContent.get("time");
			}
			events[j] = new EventDiscriptor(eventName, time, targetName, requestName);
		}
		return events;
	}

	private Options parseFrameOptions(JSONObject optionsObj) {
		if (optionsObj == null) {
			Options options = new Options(-1, -1, GrowthType.NONE, -1, -1, false, false, TransmissionType.NONE);
			return options;
		}
		// RepeatEventsOption
		long eventNumberSteps;
		long eventLinearGrowthFactor;
		GrowthType eventGrowthType;
		JSONObject repeatEventsObj = (JSONObject) optionsObj.get("repeatEvents");
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
				eventLinearGrowthFactor = (Long) repeatEventsObj.get("linearGrowthFactor");
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
		JSONObject increaseFrequencyObj = (JSONObject) optionsObj.get("increaseFrequency");
		JSONObject decreaseFrequencyObj = (JSONObject) optionsObj.get("decreaseFrequency");
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
		Options options = new Options(eventNumberSteps, eventLinearGrowthFactor, eventGrowthType, frequencySteps,
				frequencyFactor, frequencyIncrease, frequencyDecrease, transmissionType);
		return options;
	}

	private Request getSpecificRequest(JSONObject requestContent, String requestName) {
		Request request = null;
		String protocol = (String) requestContent.get("protocol");
		ProtocolType protocolType;
		if (protocol != null) {
			protocolType = ProtocolType.valueOf(protocol);
		} else {
			protocolType = ProtocolType.NONE;
		}
		switch (protocolType) {

		case HTTP:
			return request = createHttpRequest(requestContent, requestName, protocolType);
		case FTP:
			return request = createFtpRequest(requestContent, requestName, protocolType);
		case TCP:
			return request = createTcpUdpRequest(requestContent, requestName, protocolType);
		case UDP:
			return request = createTcpUdpRequest(requestContent, requestName, protocolType);
		case BFTSMaRt:
			return request = createBftsmartRequest(requestContent, requestName, protocolType);
		case NONE:
			return request;
		default:
			return request;
		}
	}

	private Request createHttpRequest(JSONObject requestContent, String requestName, ProtocolType protocolType) {
		HttpMethodType methodType;
		String methodTypeName = (String) requestContent.get("method");
		if (methodTypeName != null) {
			methodType = HttpMethodType.valueOf(methodTypeName);
		} else {
			methodType = HttpMethodType.NONE;
		}
		String resourcePath = (String) requestContent.get("resourcePath");
		String content = (String) requestContent.get("content");
		if (content == null) {
			content = "";
		}
		HttpRequest httpRequest = new HttpRequest(requestName, protocolType, methodType, resourcePath, content);
		return httpRequest;
	}

	private Request createFtpRequest(JSONObject requestContent, String requestName, ProtocolType protocolType) {
		String methodTypeName = (String) requestContent.get("method");
		FtpMethodType methodType;
		if (methodTypeName != null) {
			methodType = FtpMethodType.valueOf(methodTypeName);
		} else {
			methodType = FtpMethodType.NONE;
		}
		String localResource = (String) requestContent.get("localResource");
		String remoteResource = (String) requestContent.get("remoteResource");
		String username = (String) requestContent.get("username");
		String password = (String) requestContent.get("password");
		FtpRequest ftpRequest = new FtpRequest(requestName, protocolType, methodType, localResource, remoteResource,
				username, password);
		return ftpRequest;
	}

	private Request createTcpUdpRequest(JSONObject requestContent, String requestName, ProtocolType protocolType) {
		String content = (String) requestContent.get("content");
		TcpUdpRequest tcpUdpRequest = new TcpUdpRequest(requestName, protocolType, content);
		return tcpUdpRequest;
	}

	private Request createBftsmartRequest(JSONObject requestContent, String requestName, ProtocolType protocolType) {
		String command = (String) requestContent.get("command");
		String type = (String) requestContent.get("type");
		JSONArray targetGroupObj = (JSONArray) requestContent.get("targetGroup");
		ArrayList<String> targetGroup = new ArrayList<String>();
		for (int i = 0; i < targetGroupObj.size(); i++) {
			targetGroup.add((String) targetGroupObj.get(i));
		}
		BftsmartRequest bftsmartRequest = new BftsmartRequest(requestName, protocolType, command, type, targetGroup);
		return bftsmartRequest;
	}

}
