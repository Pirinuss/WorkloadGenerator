package wg.workload.parser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wg.requests.BftsmartCommand;
import wg.requests.BftsmartCommandType;
import wg.requests.BftsmartRequest;
import wg.requests.FtpMethodType;
import wg.requests.FtpRequest;
import wg.requests.HttpMethodType;
import wg.requests.HttpRequest;
import wg.requests.TcpRequest;
import wg.requests.UdpRequest;
import wg.workload.EventDescriptor;
import wg.workload.Frame;
import wg.workload.GrowthType;
import wg.workload.ProtocolType;
import wg.workload.Request;
import wg.workload.Schedule;
import wg.workload.Target;
import wg.workload.Workload;
import wg.workload.options.FrequencyMode;
import wg.workload.options.FrequencyOption;
import wg.workload.options.Options;
import wg.workload.options.RequestsOption;
import wg.workload.options.TransmissionType;

public class WorkloadParser {

	private static final Logger log = LoggerFactory
			.getLogger(WorkloadParser.class);

	public Workload parseWorkload(String path) throws WorkloadParserException {
		Workload workload = null;
		try {
			Object obj = new JSONParser().parse(new FileReader(path));
			JSONObject jsonObject = (JSONObject) obj;
			HashMap<String, Target[]> targets = parseTargets(jsonObject);
			HashMap<String, Request> requests = parseRequests(jsonObject);
			Schedule schedule = parseSchedule(jsonObject, targets, requests);
			workload = new Workload(targets, requests, schedule);
		} catch (FileNotFoundException cause) {
			throw new WorkloadParserException("File not found!", cause);
		} catch (ClassCastException cause) {
			throw new WorkloadParserException("Invalid data type!", cause);
		} catch (ParseException cause) {
			throw new WorkloadParserException("Invalid JSON document!", cause);
		} catch (IOException cause) {
			throw new WorkloadParserException("Could not read from file!",
					cause);
		}
		return workload;
	}

	private HashMap<String, Target[]> parseTargets(JSONObject jo)
			throws WorkloadParserException {
		JSONArray targetGroupsObj = (JSONArray) jo.get("targetGroups");
		if (targetGroupsObj == null || targetGroupsObj.size() == 0) {
			throw new WorkloadParserException(
					"No target groups found in JSON input!");
		}

		HashMap<String, Target[]> targetMap = new HashMap<String, Target[]>();

		for (int i = 0; i < targetGroupsObj.size(); i++) {
			JSONObject targetGroupObj = (JSONObject) targetGroupsObj.get(i);

			String id = (String) targetGroupObj.get("id");
			if (targetMap.containsKey(id)) {
				throw new WorkloadParserException(
						"Duplicated target group id!" + id);
			}

			JSONArray targetsObj = (JSONArray) targetGroupObj.get("targets");
			if (targetsObj == null || targetsObj.size() == 0) {
				throw new WorkloadParserException(
						"No targets found for target group " + id + " !");
			}

			Target[] targets = new Target[targetsObj.size()];
			for (int j = 0; j < targetsObj.size(); j++) {
				JSONObject targetObj = (JSONObject) targetsObj.get(j);
				String server = (String) targetObj.get("server");
				long port = -1;
				if (targetObj.get("port") != null) {
					port = (long) targetObj.get("port");
				}

				Target target = new Target(server, port);
				targets[j] = target;
			}

			targetMap.put(id, targets);
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
			if (requestMap.containsKey(id)) {
				throw new WorkloadParserException(
						"Duplicated request id!" + id);
			}
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

	private Schedule parseSchedule(JSONObject jo,
			HashMap<String, Target[]> targets,
			HashMap<String, Request> requests) throws WorkloadParserException {
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
				Frame frame = parseFrame(frameObj, targets, requests);
				frames[i] = frame;
			}

			return new Schedule(frames);
		} catch (IllegalArgumentException e) {
			throw new WorkloadParserException("Error while parsing schedule!",
					e);
		}
	}

	private Frame parseFrame(JSONObject frameObj,
			HashMap<String, Target[]> targets,
			HashMap<String, Request> requests) throws WorkloadParserException {
		String id = (String) frameObj.get("id");

		JSONArray eventsObj = (JSONArray) frameObj.get("events");
		EventDescriptor[] events = parseFrameEvents(eventsObj, targets,
				requests);

		JSONObject optionsObj = (JSONObject) frameObj.get("options");
		Options options = parseFrameOptions(optionsObj);

		Frame frame = new Frame(id, events, options);
		return frame;
	}

	private EventDescriptor[] parseFrameEvents(JSONArray eventsObj,
			HashMap<String, Target[]> targets,
			HashMap<String, Request> requests) throws WorkloadParserException {

		if (eventsObj == null || eventsObj.size() == 0) {
			throw new IllegalArgumentException(
					"No events found in JSON input!");
		}

		EventDescriptor[] events = new EventDescriptor[eventsObj.size()];
		for (int j = 0; j < eventsObj.size(); j++) {
			JSONObject eventObj = (JSONObject) eventsObj.get(j);

			String id = (String) eventObj.get("id");

			String targetName = (String) eventObj.get("target");
			Target[] target;
			if (targets.containsKey(targetName)) {
				target = targets.get(targetName);
			} else {
				if (targetName == null) {
					throw new IllegalArgumentException(
							"No target found for event: " + id);
				}
				throw new IllegalArgumentException("Target group \""
						+ targetName + "\" not found in target groups!");
			}

			String requestName = (String) eventObj.get("request");
			Request request;
			if (requests.containsKey(requestName)) {
				request = requests.get(requestName);
			} else {
				throw new IllegalArgumentException("Request \"" + requestName
						+ "\" not found in requests!");
			}

			long time = -1;
			if (eventObj.get("time") != null) {
				time = (long) eventObj.get("time");
			}

			events[j] = new EventDescriptor(id, time, target, request);
		}

		return events;
	}

	private Options parseFrameOptions(JSONObject optionsObj) {

		if (optionsObj == null) {
			throw new IllegalArgumentException(
					"No options fround in JSON input!");
		}

		TransmissionType transmissionType = parseTransmissionOption(optionsObj);

		long iterations = 1;
		if (optionsObj.get("iterations") != null) {
			iterations = (long) optionsObj.get("iterations");
		}

		JSONObject requestOptionObj = (JSONObject) optionsObj
				.get("requestsNumber");
		RequestsOption requestsOption = parseRequestsOption(requestOptionObj);

		JSONObject frequencyOptionObj = (JSONObject) optionsObj
				.get("frequency");
		FrequencyOption frequencyOption = parseFrequencyOption(
				frequencyOptionObj);

		return new Options(transmissionType, iterations, requestsOption,
				frequencyOption);
	}

	private TransmissionType parseTransmissionOption(JSONObject optionsObj) {

		TransmissionType transmissionType;
		String transmissionName = (String) optionsObj.get("transmissionType");

		if (transmissionName != null) {
			transmissionType = TransmissionType.parseString(transmissionName);
		} else {
			transmissionType = TransmissionType.PARALLEL;
		}

		return transmissionType;
	}

	private RequestsOption parseRequestsOption(JSONObject requestOptionObj) {

		RequestsOption requestsOption;
		GrowthType growthType = null;
		long linearGrowthFactor = -1;

		if (requestOptionObj == null) {
			requestsOption = new RequestsOption(GrowthType.LINEAR, 1);
		} else {
			String mode = (String) requestOptionObj.get("growth");
			if (mode != null) {
				growthType = GrowthType.parseString(mode);
			}

			if (requestOptionObj.get("linearGrowthFactor") != null) {
				linearGrowthFactor = (long) requestOptionObj
						.get("linearGrowthFactor");
			}

			requestsOption = new RequestsOption(growthType, linearGrowthFactor);
		}

		return requestsOption;
	}

	private FrequencyOption parseFrequencyOption(
			JSONObject frequencyOptionObj) {

		FrequencyOption frequencyOption;
		long factor = -1;
		FrequencyMode mode;

		if (frequencyOptionObj == null) {
			frequencyOption = new FrequencyOption(FrequencyMode.INCREASE, 1);
		} else {
			String modeName = (String) frequencyOptionObj.get("mode");
			if (modeName != null) {
				mode = FrequencyMode.parseString(modeName);
			} else {
				mode = FrequencyMode.INCREASE;
			}

			if (frequencyOptionObj.get("factor") != null) {
				factor = (long) frequencyOptionObj.get("factor");
			}

			frequencyOption = new FrequencyOption(mode, factor);
		}

		return frequencyOption;
	}

	private Request getSpecificRequest(JSONObject requestContent,
			String requestName) throws WorkloadParserException {

		long numberOfClients = 1;
		if (requestContent.get("numberOfClients") != null) {
			numberOfClients = (long) requestContent.get("numberOfClients");
		}

		String protocol = (String) requestContent.get("protocol");
		if (protocol == null) {
			throw new IllegalArgumentException(
					"Protocol not found in JSON input!");
		}

		ProtocolType protocolType = ProtocolType.fromString(protocol);
		switch (protocolType) {

		case HTTP:
			return createHttpRequest(requestContent, requestName, protocolType,
					numberOfClients);
		case FTP:
			return createFtpRequest(requestContent, requestName, protocolType,
					numberOfClients);
		case TCP:
			return createTcpRequest(requestContent, requestName, protocolType,
					numberOfClients);
		case UDP:
			return createUdpRequest(requestContent, requestName, protocolType,
					numberOfClients);
		case BFTSMaRt:
			return createBftsmartRequest(requestContent, requestName,
					protocolType, numberOfClients);
		default:
			return null;
		}

	}

	private Request createHttpRequest(JSONObject requestContent,
			String requestName, ProtocolType protocolType, long numberOfClients)
			throws WorkloadParserException {

		String resourcePath = (String) requestContent.get("resourcePath");
		String content = (String) requestContent.get("content");

		String methodTypeName = (String) requestContent.get("method");
		if (methodTypeName == null) {
			throw new IllegalArgumentException(
					"Method not found in JSON input!");
		}
		HttpMethodType methodType = HttpMethodType.fromString(methodTypeName);

		return new HttpRequest(requestName, protocolType, numberOfClients,
				methodType, resourcePath, content);
	}

	private Request createFtpRequest(JSONObject requestContent,
			String requestName, ProtocolType protocolType, long numberOfClients)
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

		return new FtpRequest(requestName, protocolType, numberOfClients,
				methodType, localResource, remoteResource, username, password);
	}

	private Request createTcpRequest(JSONObject requestContent,
			String requestName, ProtocolType protocolType,
			long numberOfClients) {

		String content = (String) requestContent.get("content");
		return new TcpRequest(requestName, protocolType, numberOfClients,
				content);
	}

	private Request createUdpRequest(JSONObject requestContent,
			String requestName, ProtocolType protocolType,
			long numberOfClients) {

		String content = (String) requestContent.get("content");
		return new UdpRequest(requestName, protocolType, numberOfClients,
				content);
	}

	private Request createBftsmartRequest(JSONObject requestContent,
			String requestName, ProtocolType protocolType, long numberOfClients)
			throws WorkloadParserException {

		JSONObject command = (JSONObject) requestContent.get("command");
		BftsmartCommand bftsmartCommand = parseBFTSMaRtCommand(command);
		String type = (String) requestContent.get("type");

		return new BftsmartRequest(requestName, protocolType, numberOfClients,
				bftsmartCommand, type);
	}

	private BftsmartCommand parseBFTSMaRtCommand(JSONObject command) {

		String type = (String) command.get("type");
		if (type == null) {
			throw new IllegalArgumentException(
					"BFTSMaRt content type not found in JSON input!");
		}
		BftsmartCommandType commandType = BftsmartCommandType.fromString(type);

		String content = (String) command.get("content");
		Object[] objects = null;

		if (commandType == BftsmartCommandType.BYTE_OBJECT_STREAM) {
			String[] params = content.split(",");
			if (params.length == 0) {
				throw new IllegalArgumentException("Invalid BFTSMaRt content!");
			}
			objects = new Object[params.length];
			for (int i = 0; i < params.length; i++) {
				String[] param = params[i].split(":");
				if (param.length != 2) {
					throw new IllegalArgumentException(
							"Invalid BFTSMaRt content!");
				}
				String id = param[0];
				String identifier = param[1];
				if (command.get(id) == null) {
					throw new IllegalArgumentException(
							"No parameter found for BFTSMaRt content! " + id);
				}
				try {
					objects[i] = getObject(identifier,
							command.get(id).toString());
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(
							"Invalid parameter for BFTSMaRt content! " + id);
				}
			}
		}
		return new BftsmartCommand(commandType, content, objects);
	}

	private static Object getObject(String identifier, String stringParam) {
		Object object;
		switch (identifier.toUpperCase()) {
		case "BOOLEAN":
			if (!stringParam.toUpperCase().equals("TRUE")
					&& !stringParam.toUpperCase().equals("FALSE")) {
				throw new IllegalArgumentException("Invalid parameter!");
			}
			object = Boolean.valueOf(stringParam);
			break;
		case "CHAR":
			if (stringParam.length() != 1) {
				throw new IllegalArgumentException("Invalid parameter!");
			}
			object = Character.valueOf(stringParam.charAt(0));
			break;
		case "BYTE":
			object = Byte.valueOf(stringParam);
			break;
		case "SHORT":
			object = Short.valueOf(stringParam);
			break;
		case "INT":
			object = Integer.valueOf(stringParam);
			break;
		case "LONG":
			object = Long.valueOf(stringParam);
			break;
		case "FLOAT":
			object = Float.valueOf(stringParam);
			break;
		case "DOUBLE":
			object = Double.valueOf(stringParam);
			break;
		case "STRING":
			object = String.valueOf(stringParam);
			break;
		default:
			throw new IllegalArgumentException(
					"Unknown identifier" + identifier);
		}
		return object;
	}

}
