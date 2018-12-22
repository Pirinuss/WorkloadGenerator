package wg.core;

import wg.workload.Request;
import wg.workload.Target;

public class Response {
	
	Target target;
	Request request;
	String responseInfos;
	String responseContent;
	long eventStartTime;
	long eventStopTime;
	
	public Target getTarget() {
		return target;
	}
	public void setTarget(Target target) {
		this.target = target;
	}
	public Request getRequest() {
		return request;
	}
	public void setRequest(Request request) {
		this.request = request;
	}
	public long getEventStartTime() {
		return eventStartTime;
	}
	public void setEventStartTime(long eventStartTime) {
		this.eventStartTime = eventStartTime;
	}
	public long getEventStopTime() {
		return eventStopTime;
	}
	public void setEventStopTime(long eventStopTime) {
		this.eventStopTime = eventStopTime;
	}
	public String getResponseInfos() {
		return responseInfos;
	}
	public void setResponseInfos(String responseInfos) {
		this.responseInfos = responseInfos;
	}
	public String getResponseContent() {
		return responseContent;
	}
	public void setResponseContent(String responseContent) {
		this.responseContent = responseContent;
	}
	
}
