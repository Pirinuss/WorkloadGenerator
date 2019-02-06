package wg.workload.options;

import wg.workload.GrowthType;

public class RequestsOption {

	private final GrowthType growthType;
	private final long linearGrowthFactor;
	private final long steps;

	public RequestsOption(GrowthType growthType, long linearGrowthFactor,
			long steps) {

		if (growthType == null) {
			throw new IllegalArgumentException("Growth type must not be null!");
		}
		this.growthType = growthType;

		if (linearGrowthFactor < 1 && growthType == GrowthType.LINEAR) {
			throw new IllegalArgumentException("Invalid growth factor!");
		}
		if (linearGrowthFactor > 0 && !(growthType == GrowthType.LINEAR)) {
			throw new IllegalArgumentException("No \"linearGrowthFactor\" required for this growth type!");
		}
		this.linearGrowthFactor = linearGrowthFactor;

		if (steps < 1) {
			throw new IllegalArgumentException("Invalid step value!");
		}
		this.steps = steps;
	}

	public GrowthType getGrowthType() {
		return growthType;
	}

	public long getLinearGrowthFactor() {
		return linearGrowthFactor;
	}

	public long getSteps() {
		return steps;
	}

}
