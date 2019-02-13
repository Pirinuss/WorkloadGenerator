package wg.workload;

import java.util.HashMap;
import java.util.Map.Entry;

public class Workload {

	private final HashMap<String, Target[]> targets;
	private final HashMap<String, Request> requests;
	private final Schedule schedule;

	public Workload(HashMap<String, Target[]> targets,
			HashMap<String, Request> requests, Schedule schedule) {
		if (targets == null) {
			throw new IllegalArgumentException("No targets found");
		}
		this.targets = targets;

		if (requests == null) {
			throw new IllegalArgumentException("No requests found");
		}
		this.requests = requests;

		if (schedule == null) {
			throw new IllegalArgumentException("No schedule found");
		}
		this.schedule = schedule;

		for (Entry<String, Target[]> entry : targets.entrySet()) {
			if (entry.getValue() == null) {
				throw new IllegalArgumentException(
						entry.getKey() + " not found");
			}
		}

		for (Entry<String, Request> entry : requests.entrySet()) {
			if (entry.getValue() == null) {
				throw new IllegalArgumentException(
						entry.getKey() + " not found");
			}
		}
	}

	public HashMap<String, Target[]> getTargets() {
		return targets;
	}

	public HashMap<String, Request> getRequests() {
		return requests;
	}

	public Schedule getSchedule() {
		return schedule;
	}

}
