package wg.workload;

public enum FrameModeType {
	
	DEFINEDTIME,
	REPEAT,
	INCREASEEXPO,
	INCREASEFIB;
	
	public static FrameModeType parseString(String mode) {
		switch (mode) {
		case "repeat":
			return REPEAT;
		case "definedTime":
			return DEFINEDTIME;
		case "increaseExpo":
			return INCREASEEXPO;
		case "increaseFib":
			return INCREASEFIB;
		default:
			return null;
		}
	}

}
