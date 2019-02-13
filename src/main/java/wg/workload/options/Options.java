package wg.workload.options;

import wg.workload.GrowthType;

public class Options {

	private final TransmissionType transmissionType;
	private final long iterations;
	private final RequestsOption requestsOption;
	private final FrequencyOption frequencyOption;

	public Options(TransmissionType transmissionType, long iterations,
			RequestsOption requestsOption, FrequencyOption frequencyOption) {

		if (transmissionType == null) {
			this.transmissionType = TransmissionType.PARALLEL;
		} else {
			this.transmissionType = transmissionType;
		}

		if (iterations < 1) {
			throw new IllegalArgumentException(
					"At least one iteration required!");
		}
		this.iterations = iterations;

		if (requestsOption == null) {
			this.requestsOption = new RequestsOption(GrowthType.LINEAR, 1);
		} else {
			this.requestsOption = requestsOption;
		}

		if (frequencyOption == null) {
			this.frequencyOption = new FrequencyOption(FrequencyMode.INCREASE,
					1);
		} else {
			this.frequencyOption = frequencyOption;
		}

	}

	public TransmissionType getTransmissionType() {
		return transmissionType;
	}

	public long getIterations() {
		return iterations;
	}

	public RequestsOption getRequestsOption() {
		return requestsOption;
	}

	public FrequencyOption getFrequencyOption() {
		return frequencyOption;
	}

}
