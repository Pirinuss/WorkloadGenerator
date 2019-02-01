package wg.workload;

public enum ProtocolType {

	HTTP, FTP, TCP, UDP, BFTSMaRt;

	public static ProtocolType fromString(String identifier) {
		switch (identifier.toUpperCase()) {
		case "HTTP":
			return HTTP;
		case "FTP":
			return FTP;
		case "BFTSMART":
			return BFTSMaRt;
		case "TCP":
			return TCP;
		case "UDP":
			return UDP;
		default:
			throw new IllegalArgumentException(
					"Unknown identifier! " + identifier);
		}
	}

}
