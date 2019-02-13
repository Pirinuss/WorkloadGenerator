package wg.workload.options;

import wg.workload.GrowthType;

public class RequestsOption {

	private final GrowthType growthType;
	private final long linearGrowthFactor;

	public RequestsOption(GrowthType growthType, long linearGrowthFactor) {

		if (growthType == null) {
			throw new IllegalArgumentException("Growth type must not be null!");
		}
		this.growthType = growthType;

		if (linearGrowthFactor < 1 && growthType == GrowthType.LINEAR) {
			throw new IllegalArgumentException("Invalid growth factor!");
		}
		if (linearGrowthFactor > 0 && !(growthType == GrowthType.LINEAR)) {
			throw new IllegalArgumentException(
					"No \"linearGrowthFactor\" required for this growth type!");
		}
		this.linearGrowthFactor = linearGrowthFactor;
	}

	public GrowthType getGrowthType() {
		return growthType;
	}

	public long getLinearGrowthFactor() {
		return linearGrowthFactor;
	}

}
