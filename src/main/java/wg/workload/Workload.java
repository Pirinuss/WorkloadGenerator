package wg.workload;

import java.util.HashMap;
import java.util.Map.Entry;

public class Workload {

	private final HashMap<String, Target> targets;
	private final HashMap<String, Request> requests;
	private final Schedule schedule;
	
	public Workload(HashMap<String, Target> targets, HashMap<String, Request> requests, Schedule schedule) {
		this.targets = targets;
		this.requests = requests;
		this.schedule = schedule;
	}

	public HashMap<String, Target> getTargets() {
		return targets;
	}
	
	public HashMap<String, Request> getRequests() {
		return requests;
	}
	
	public Schedule getSchedule() {
		return schedule;
	}
	
	public Target getTargetByName(String targetName) {
		for (Entry<String, Target> e : targets.entrySet()) {
			if (e.getKey().equals(targetName)) {
				Target target = e.getValue();
				return target;
			}
		}
		return null;
	}
	
}
