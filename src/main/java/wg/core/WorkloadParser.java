package wg.core;

import wg.requests.FtpRequest;
import wg.requests.FtpMethodType;
import wg.requests.HttpMethodType;
import wg.requests.HttpRequest;
import wg.workload.EventDiscriptor;
import wg.workload.Frame;
import wg.workload.ProtocolType;
import wg.workload.Request;
import wg.workload.Schedule;
import wg.workload.Target;
import wg.workload.Workload;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import org.json.simple.JSONArray; 
import org.json.simple.JSONObject; 
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class WorkloadParser {

	public Workload parseWorkload(String path) {
		Workload w = null;
		try {
			Object obj = new JSONParser().parse(new FileReader(path));
			JSONObject jo = (JSONObject) obj;
			HashMap<String, Target> targets = parseTargets(jo);
			HashMap<String, Request> requests = parseRequests(jo);
			Schedule schedule = parseSchedule(jo);
			w = new Workload();
			w.setTargets(targets);
			w.setRequests(requests);
			w.setSchedule(schedule);
		} catch (FileNotFoundException e ) {
			//TODO Fehlerbehandlung nicht via Exception
			System.out.println("File not found!");	
		} catch (ParseException e) {
			System.out.println("Invalid JSON Document!");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return w;
	}
	
	/**
	 * Liest die Targets aus dem JSON Dokument aus
	 * @param jo Das JSON Dokument als JSONObject
	 * @return targetMap Die HashMap mit den Targetnamen und 
	 * 					 den ausgelesenen Targets 
	 */
	private HashMap<String, Target> parseTargets(JSONObject jo) {
		JSONArray targets = (JSONArray) jo.get("targets");
		if (targets != null) {
			HashMap<String, Target> targetMap = new HashMap<String, Target>();
			for (int i=0; i<targets.size(); i++) {
				Target newTarget = new Target();
				JSONObject targetObj = (JSONObject) targets.get(i);
				String targetName = ("target").concat(String.valueOf(i+1));
				JSONObject targetContent = (JSONObject) targetObj.get(targetName);
				String servername = (String) targetContent.get("servername");
				String port = (String) targetContent.get("port");
				newTarget.setTargetName(targetName);
				newTarget.setServerName(servername);
				newTarget.setPort(port);
				targetMap.put(targetName, newTarget);
			}
			return targetMap;
		}
		return null;
	}
	
	/**
	 * Liest die Requests aus dem JSON Dokument aus
	 * @param jo Das JSON Dokument als JSONObject
	 * @return requestMap Die HashMap mit den Requestnamen und 
	 * 					 den ausgelesenen Requests 
	 */
	private HashMap<String, Request> parseRequests(JSONObject jo) {
		JSONArray requests = (JSONArray) jo.get("requests");
		if (requests != null) {
			HashMap<String, Request> requestMap = new HashMap<String, Request>();
			for (int i=0; i<requests.size(); i++) {
				Request newRequest;
				JSONObject requestObj = (JSONObject) requests.get(i);
				String requestName = ("request").concat(String.valueOf(i+1));
				JSONObject requestContent = (JSONObject) requestObj.get(requestName);
				newRequest = getSpecificRequest(requestContent);
				requestMap.put(requestName, newRequest);
			}
			return requestMap;
		}
		return null;
	}
	
	/**
	 * Liest den Schedule aus dem JSON Dokument aus
	 * @param jo Das JSON Dokument als JSONObject
	 * @return schedule Der Schedule mit allen Frames und allen Events 
	 */
	private Schedule parseSchedule(JSONObject jo) {
		Schedule schedule = new Schedule();
		JSONObject scheduleObj = (JSONObject) jo.get("schedule");
		JSONArray framesObj = (JSONArray) scheduleObj.get("frames");
		Frame[] frames = new Frame[framesObj.size()];
		for (int i=0; i<framesObj.size(); i++) {
			Frame frame = new Frame();
			JSONObject frameObj = (JSONObject) framesObj.get(i);
			String frameName = ("frame").concat(String.valueOf(i+1));
			frame.setFrameName(frameName);
			JSONObject frameContent = (JSONObject) frameObj.get(frameName);
			JSONArray eventsObj = (JSONArray) frameContent.get("events");
			EventDiscriptor[] events = new EventDiscriptor[eventsObj.size()];
			for (int j=0; j<eventsObj.size(); j++) {
				EventDiscriptor event = new EventDiscriptor();
				JSONObject eventObj = (JSONObject) eventsObj.get(j);
				String eventName = ("event").concat(String.valueOf(j+1));
				JSONObject eventContent = (JSONObject) eventObj.get(eventName);
				event.setEventName(eventName);
				String targetName = (String) eventContent.get("target");
				event.setTargetName(targetName);
				String requestName = (String) eventContent.get("request");
				event.setRequestName(requestName);
				long time = (Long) eventContent.get("time");
				event.setTime(time);
				events[j] = event; 
			}
			frame.setEvents(events);
			frames[i] = frame;
		}
		schedule.setFrames(frames);
		return schedule;
	}
	
	/**
	 * Mappt das Protokoll auf den spezifischen Request und lässt 
	 * diesen erzeugen
	 * @param requestContent Das JSON Objekt aus dem JSON Dokument mit den
	 * 		  einzelnen Request Parametern
	 * @return Das ensprechende Request Objekt
	 */
	private Request getSpecificRequest(JSONObject requestContent) {
		Request request = null;
		String protocol = (String) requestContent.get("protocol");
		ProtocolType protocolType = ProtocolType.valueOf(protocol);
		switch (protocolType) {
			
		case HTTP:
			return request = createHttpRequest(requestContent);
		case FTP:
			return request = createFtpRequest(requestContent);
		case TCP:
			//TODO Konstruieren und Rückgeben eines neuen TcpRequest 
			return request;
		case UDP:
			//TODO Konstruieren und Rückgeben eines neuen UdpRequest 
			return request;
		default:
			return request;
				
		}
	}
	
	/**
	 * Erzeugt einen neuen HTTP Request, füllt ihn mit den
	 * Werten aus dem JSON Objekt und gibt ihn zurück
	 * @param requestContent Das JSON Objekt aus dem JSON
	 * Dokument welches die einzelnen Parameter des Requests
	 * enthält
	 * @return httpRequest Der erzeugte und befüllte Request
	 */
	private Request createHttpRequest(JSONObject requestContent) {
		HttpRequest httpRequest = new HttpRequest();
		httpRequest.setProtocol(ProtocolType.HTTP);
		String methodTypeName = (String) requestContent.get("method");
		HttpMethodType methodType = HttpMethodType.valueOf(methodTypeName);
		httpRequest.setMethod(methodType);
		String resourcePath = (String) requestContent.get("resourcePath");
		httpRequest.setResourcePath(resourcePath);
		String content = (String) requestContent.get("content");
		httpRequest.setContent(content.getBytes());
		return httpRequest;
	}
	
	/**
	 * Erzeugt einen neuen FTP Request, füllt ihn mit den
	 * Werten aus dem JSON Objekt und gibt ihn zurück
	 * @param requestContent Das JSON Objekt aus dem JSON
	 * Dokument welches die einzelnen Parameter des Requests
	 * enthält
	 * @return ftpRequest Der erzeugte und befüllte Request
	 */
	private Request createFtpRequest(JSONObject requestContent) {
		FtpRequest ftpRequest = new FtpRequest();
		ftpRequest.setProtocol(ProtocolType.FTP);
		String methodTypeName = (String) requestContent.get("method");
		FtpMethodType methodType = FtpMethodType.valueOf(methodTypeName);
		ftpRequest.setMethod(methodType);
		String localResource = (String) requestContent.get("localResource");
		ftpRequest.setLocalResource(localResource);
		String remoteResource = (String) requestContent.get("remoteResource");
		ftpRequest.setRemoteResource(remoteResource);
		String username = (String) requestContent.get("username");
		ftpRequest.setUsername(username);
		String password = (String) requestContent.get("password");
		ftpRequest.setPassword(password);
		return ftpRequest;
	}
	
}
