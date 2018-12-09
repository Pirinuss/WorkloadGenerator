package wg.workload;

import java.util.HashMap;

public class Workload {

	private HashMap<String, Target> targets;
	private HashMap<String, Request> requests;
	private Schedule schedule;
	
	public Workload(HashMap<String, Target> targets, HashMap<String, Request> requests, Schedule schedule) {
		this.targets = targets;
		this.requests = requests;
		this.schedule = schedule;
	}
	
}
