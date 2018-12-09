package wg.core;

import wg.requests.HttpMethodType;
import wg.requests.HttpRequest;
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
			//***DEBUG***
			System.out.println(requests.size());
			HttpRequest request = (HttpRequest) requests.get("request2");
			System.out.println(request.getProtocol());
			System.out.println(request.getMethod());
			System.out.println(request.getResourcePath());
			System.out.println(new String(request.getContent()));
			//***DEBUG ENDE***
			Schedule schedule = parseSchedule(jo);
			w = new Workload(targets, requests, schedule);
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
				newTarget.setServername(servername);
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
	
	//Liest den Schedule aus dem JSON Dokument aus
	private Schedule parseSchedule(JSONObject jo) {
		return null;
	}
	
	/**
	 * Erzeugt je nach angegebenem Protokoll den entsprechenden Request 
	 * und gibt diesen zurück
	 * @param requestContent Das JSON Objekt aus dem JSON Dokument mit den
	 * 		  einzelnen Request Parametern
	 * @return Das ensprechende Request Objekt
	 */
	private Request getSpecificRequest(JSONObject requestContent) {
		String protocol = (String) requestContent.get("protocol");
		ProtocolType protocolType = ProtocolType.valueOf(protocol);
		switch (protocolType) {
			
		case HTTP:
			HttpRequest httpRequest = new HttpRequest();
			httpRequest.setProtocol(ProtocolType.HTTP);
			String methodTypeName = (String) requestContent.get("method");
			HttpMethodType methodType = HttpMethodType.valueOf(methodTypeName);
			switch (methodType) {
			case GET:
				httpRequest.setMethod(HttpMethodType.GET);
				break;
			case DELETE:
				httpRequest.setMethod(HttpMethodType.DELETE);
				break;
			case POST:
				httpRequest.setMethod(HttpMethodType.POST);
				break;
			case PUT:
				httpRequest.setMethod(HttpMethodType.PUT);
				break;
			default:
				httpRequest.setMethod(null);
	
			}
			String resourcePath = (String) requestContent.get("resourcePath");
			httpRequest.setResourcePath(resourcePath);
			String content = (String) requestContent.get("content");
			if (content != null) {
				httpRequest.setContent(content.getBytes());
			}
			return httpRequest;
		case FTP:
			//TODO Konstruieren und Rückgeben eines neuen FtpRequest 
			return null;
		case TCP:
			//TODO Konstruieren und Rückgeben eines neuen TcpRequest 
			return null;
		case UDP:
			//TODO Konstruieren und Rückgeben eines neuen UdpRequest 
			return null;
		default:
			return null;
				
		}
	}
	
}
