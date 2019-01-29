package wg.workload;

public class Options {

	private final long eventNumberSteps;
	private final long eventLinearGrowthFactor;
	private final GrowthType eventGrowthType;
	private final long frequencySteps;
	private final long frequencyFactor;
	private final boolean frequencyIncrease;
	private final boolean frequencyDecrease;
	private final TransmissionType transmissionType;

	public Options(long eventNumberSteps, long eventLinearGrowthFactor, GrowthType eventGrowthType, long frequencySteps,
			long frequencyFactor, boolean frequencyIncrease, boolean frequencyDecrease,
			TransmissionType transmissionType) {
		this.eventNumberSteps = eventNumberSteps;
		this.eventLinearGrowthFactor = eventLinearGrowthFactor;
		this.eventGrowthType = eventGrowthType;
		this.frequencySteps = frequencySteps;
		this.frequencyFactor = frequencyFactor;
		this.frequencyIncrease = frequencyIncrease;
		this.frequencyDecrease = frequencyDecrease;
		this.transmissionType = transmissionType;
		if (eventGrowthType == GrowthType.LINEAR && eventLinearGrowthFactor == -1) {
			throw new IllegalArgumentException("\"LinearGrowthFactor\" is missing");
		}
		if (eventGrowthType == GrowthType.INCREASEEXPO || eventGrowthType == GrowthType.INCREASEFIB) {
			if (eventLinearGrowthFactor != -1) {
				throw new IllegalArgumentException(
						"The option \"repeatEvents\" doesn´t need a \"linearGrowthFactor\" for the mode " + "\""
								+ GrowthType.parseGrowthType(eventGrowthType) + "\"");
			}
		}
		if (frequencyIncrease == true && frequencyDecrease == true) {
			throw new IllegalArgumentException("There can´t be a frequency increase and a decrease at the same time");
		}
		if (frequencyIncrease == true || frequencyDecrease == true) {
			if (frequencySteps == -1) {
				throw new IllegalArgumentException("\"steps\" is missing for the frequency mode option");
			}
			if (frequencyFactor == -1) {
				throw new IllegalArgumentException("\"factor\" is missing for the frequency mode option");
			}
		}
	}

	public long getEventNumberSteps() {
		return eventNumberSteps;
	}

	public long getEventLinearGrowthFactor() {
		return eventLinearGrowthFactor;
	}

	public GrowthType getEventGrowthType() {
		return eventGrowthType;
	}

	public long getFrequencySteps() {
		return frequencySteps;
	}

	public long getFrequencyFactor() {
		return frequencyFactor;
	}

	public boolean isFrequencyIncrease() {
		return frequencyIncrease;
	}

	public boolean isFrequencyDecrease() {
		return frequencyDecrease;
	}

	public TransmissionType getTransmissionType() {
		return transmissionType;
	}

}
