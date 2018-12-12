package wg.workload;

import java.util.HashMap;

public class Workload {

	private HashMap<String, Target> targets;
	private HashMap<String, Request> requests;
	private Schedule schedule;
	
	public HashMap<String, Target> getTargets() {
		return targets;
	}
	public void setTargets(HashMap<String, Target> targets) {
		this.targets = targets;
	}
	public HashMap<String, Request> getRequests() {
		return requests;
	}
	public void setRequests(HashMap<String, Request> requests) {
		this.requests = requests;
	}
	public Schedule getSchedule() {
		return schedule;
	}
	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}
	
	
	
}
