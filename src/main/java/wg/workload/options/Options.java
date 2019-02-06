package wg.workload.options;

import wg.workload.GrowthType;

public class Options {

	private final TransmissionType transmissionType;
	private final Clients clients;
	private final RequestsOption requestsOption;
	private final FrequencyOption frequencyOption;

	public Options(TransmissionType transmissionType,
			Clients clients, RequestsOption requestsOption,
			FrequencyOption frequencyOption) {

		if (transmissionType == null) {
			this.transmissionType = TransmissionType.PARALLEL;
		} else {
			this.transmissionType = transmissionType;
		}

		if (clients == null) {
			this.clients = new Clients(1, 1, 1, 1, 1);
		} else {
			this.clients = clients;
		}

		if (requestsOption == null) {
			this.requestsOption = new RequestsOption(GrowthType.LINEAR, 1, 1);
		} else {
			this.requestsOption = requestsOption;
		}

		if (frequencyOption == null) {
			this.frequencyOption = new FrequencyOption(FrequencyMode.INCREASE,
					1, 1);
		} else {
			this.frequencyOption = frequencyOption;
		}

	}

	public TransmissionType getTransmissionType() {
		return transmissionType;
	}

	public Clients getClients() {
		return clients;
	}

	public RequestsOption getRequestsOption() {
		return requestsOption;
	}

	public FrequencyOption getFrequencyOption() {
		return frequencyOption;
	}

}
