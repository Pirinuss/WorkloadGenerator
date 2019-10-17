package wg.parser;

import org.json.simple.JSONObject;

import wg.requests.FtpRequest;
import wg.requests.HttpRequest;
import wg.requests.Request;
import wg.requests.TcpRequest;
import wg.requests.UdpRequest;
import wg.requests.bftsmart.BftsmartRequest;

public enum ProtocolType {

	TCP, UDP, HTTP, FTP, BFTSMART;

	public static Request getRequestFromString(String identifier,
			JSONObject requestContent) throws WorkloadParserException {
		switch (identifier.toLowerCase()) {
		case "http":
			return new HttpRequest(requestContent);
		case "ftp":
			return new FtpRequest(requestContent);
		case "tcp":
			return new TcpRequest(requestContent);
		case "udp":
			return new UdpRequest(requestContent);
		case "bftsmart":
			return new BftsmartRequest(requestContent);
		default:
			throw new IllegalArgumentException("Unknown protocol");
		}
	}

}
