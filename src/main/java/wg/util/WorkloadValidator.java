package wg.util;

import java.util.HashMap;
import java.util.Map.Entry;

import wg.requests.FtpRequest;
import wg.requests.HttpMethodType;
import wg.requests.HttpRequest;
import wg.requests.TcpUdpRequest;
import wg.workload.EventDiscriptor;
import wg.workload.Frame;
import wg.workload.Request;
import wg.workload.Schedule;
import wg.workload.Target;
import wg.workload.Workload;

public class WorkloadValidator {
	
	Workload w;
	
	public boolean validateWorkload(Workload workload) {
		this.w = workload;
		if (w == null) {
			return false;
		}
		if (w.getTargets() == null) {
			System.out.println("No targets found");
			return false;
		}
		if (w.getRequests() == null) {
			System.out.println("No requests found");
			return false;
		}
		if (w.getSchedule() == null) {
			System.out.println("No schedule found");
			return false;
		}
		if (validateTargets(w.getTargets())) {
			if (validateRequests(w.getRequests())) {
				if (validateSchedule(w.getSchedule())) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean validateTargets(HashMap<String, Target> targets) {
		for (Entry<String, Target> entry : targets.entrySet()) {
			if (entry.getValue() == null) {
				System.out.println("Target: " + entry.getKey() + " was not found. Please check if the targets are in a correct order.");
				return false;
			}
			if (entry.getValue().getServerName() == null) {
				System.out.println("No server name found for: " + entry.getKey());
				return false;
			}
		}
		return true;
	}
	
	private boolean validateRequests(HashMap<String, Request> requests) {
		for (Entry<String, Request> entry : requests.entrySet()) {
			if (entry.getValue() == null ) {
				System.out.println("Request: " + entry.getKey() + " was not found. Please check if the requests are in a correct order.");
				return false;
			}
			switch (entry.getValue().getProtocol()) {
			case FTP:
				FtpRequest ftpRequest = (FtpRequest) entry.getValue();
				if (!validateFtpRequest(ftpRequest)) {
					return false;
				}
				break;
			case HTTP:
				HttpRequest httpRequest = (HttpRequest) entry.getValue();
				if (!validateHttpRequest(httpRequest)) {
					return false;
				}
				break;
			case TCP:
				TcpUdpRequest tcpRequest = (TcpUdpRequest) entry.getValue();
				if (!validateTcpUdpRequest(tcpRequest)) {
					return false;
				}
				break;
			case UDP:
				TcpUdpRequest udpRequest = (TcpUdpRequest) entry.getValue();
				if (!validateTcpUdpRequest(udpRequest)) {
					return false;
				}
				break;
			case NONE:
				System.out.println("No protocol defined for request: " + entry.getKey());
				return false;
			default:
				break;
			
			}
		}
		return true;
	}
	
	private boolean validateSchedule(Schedule schedule) {
		Frame[] frames = schedule.getFrames();
		if (frames.length == 0) {
			System.out.println("No frames found");
			return false;
		}
		for (int i=0; i<frames.length; i++) {
			if (frames[i] == null) {
				System.out.println("Frame" + i+1 + " was not found. Please check if the frames are in a correct order.");
				return false;
			}
			EventDiscriptor[] events = frames[i].getEvents();
			if (events.length == 0) {
				System.out.println("No events found for " + frames[i].getFrameName());
				return false;
			}
			for (int j=0; j<events.length; j++) {
				if (events[j] == null) {
					System.out.println("Event" + i+1 + " of " + frames[i].getFrameName() + " was not found. Please check if the events are in a correct order.");
					return false;
				}
				if (events[j].getTargetName() == null) {
					System.out.println("No target defined for " + events[j].getEventName() + " of " + frames[i].getFrameName()+ ".");
					return false;
				}
				if (events[j].getRequestName() == null) {
					System.out.println("No request defined for " + events[j].getEventName() + " of " + frames[i].getFrameName()+ ".");
					return false;
				}
				if (events[j].getTime() == -1) {
					System.out.println("No execution time defined for " + events[j].getEventName() + " of " + frames[i].getFrameName()+ ".");
					return false;
				}
				if (!w.getTargets().containsKey(events[j].getTargetName())) {
					System.out.println(events[j].getTargetName() + " does not exist. (" + frames[i].getFrameName() + ", " + events[j].getEventName() + ")");
					return false;
				}
				if (!w.getRequests().containsKey(events[j].getRequestName())) {
					System.out.println(events[j].getRequestName() + " does not exist. (" + frames[i].getFrameName() + ", " + events[j].getEventName() + ")");
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean validateFtpRequest(FtpRequest request) {
		if (request.getMethod() == null) {
			System.out.println("FTP method not defined for request: " + request.getRequestName());
			return false;
		}
		if (request.getLocalResource() == null) {
			System.out.println("Local resource not defined for request: " + request.getLocalResource());
			return false;
		}
		if (request.getRemoteResource() == null) {
			System.out.println("Remote resource not defined for request: " + request.getRemoteResource());
			return false;
		}
		return true;
	}
	
	private boolean validateHttpRequest(HttpRequest request) {
		if (request.getMethod() == HttpMethodType.NONE) {
			System.out.println("HTTP method not defined for request: " + request.getRequestName());
			return false;
		}
		if (request.getMethod() == HttpMethodType.POST || request.getMethod() == HttpMethodType.PUT) {
			if (request.getContent() == null) {
				System.out.println("Content not defined for request: " + request.getRequestName());
				return false;
			}
		}
		return true;
	}
	
	private boolean validateTcpUdpRequest(TcpUdpRequest request) {
		if (request.getContent() == null) {
			System.out.println("Content not defined for request: " + request.getRequestName());
			return false;
		}
		return true;
	}
	
}
