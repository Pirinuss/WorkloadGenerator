package wg.core;

import java.util.concurrent.Callable;

import wg.requests.BftsmartRequest;
import wg.requests.FtpRequest;
import wg.requests.HttpRequest;
import wg.requests.TcpRequest;
import wg.requests.UdpRequest;
import wg.workload.Request;
import wg.workload.Target;

public class Event implements Callable<Response[]> {

	private final Target[] targetGroup;
	private final Request request;

	public Event(Target[] targetGroup, Request request) {
		this.targetGroup = targetGroup;
		this.request = request;
	}

	public Response[] call() throws WorkloadExecutionException {

		switch (request.getProtocol()) {

		case HTTP:
			HttpRequest httpRequest = (HttpRequest) request;
			return httpRequest.execute(targetGroup);
		case FTP:
			FtpRequest ftpRequest = (FtpRequest) request;
			return ftpRequest.execute(targetGroup);
		case TCP:
			TcpRequest tcpRequest = (TcpRequest) request;
			return tcpRequest.execute(targetGroup);
		case UDP:
			UdpRequest udpRequest = (UdpRequest) request;
			return udpRequest.execute(targetGroup);
		case BFTSMaRt:
			BftsmartRequest bftRequest = (BftsmartRequest) request;
			return bftRequest.execute(targetGroup);
		default:
			return null;
		}

	}

}
