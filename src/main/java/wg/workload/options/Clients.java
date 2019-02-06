package wg.workload.options;

import java.net.DatagramSocket;
import java.net.Socket;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.http.client.HttpClient;

import bftsmart.tom.ServiceProxy;
import wg.workload.ProtocolType;

public class Clients {

	private final HttpClient[] httpClients;
	private final FTPClient[] ftpClients;
	private final ServiceProxy[] bftsmartClients;
	private final Socket[] tcpClients;
	private final DatagramSocket[] udpClients;

	public Clients(long bftsmartClients, long httpClients, long ftpClients,
			long udpClients, long tcpClients) {

		if (bftsmartClients < 1) {
			throw new IllegalArgumentException(
					"At least one BFTSMaRt client is required!");
		}
		this.bftsmartClients = new ServiceProxy[(int) bftsmartClients];

		if (httpClients < 1) {
			throw new IllegalArgumentException(
					"At least one HTTP client is required!");
		}
		this.httpClients = new HttpClient[(int) httpClients];

		if (ftpClients < 1) {
			throw new IllegalArgumentException(
					"At least one FTP client is required!");
		}
		this.ftpClients = new FTPClient[(int) ftpClients];

		if (udpClients < 1) {
			throw new IllegalArgumentException(
					"At least one UDP client is required!");
		}
		this.udpClients = new DatagramSocket[(int) udpClients];

		if (tcpClients < 1) {
			throw new IllegalArgumentException(
					"At least one TCP client is required!");
		}
		this.tcpClients = new Socket[(int) tcpClients];

	}

	public HttpClient[] getHttpClients() {
		return httpClients;
	}

	public FTPClient[] getFtpClients() {
		return ftpClients;
	}

	public ServiceProxy[] getBftsmartClients() {
		return bftsmartClients;
	}

	public Socket[] getTcpClients() {
		return tcpClients;
	}

	public DatagramSocket[] getUdpClients() {
		return udpClients;
	}

	public int getClientsNumber(ProtocolType protocol) {
		switch (protocol) {
		case BFTSMaRt:
			return bftsmartClients.length;
		case FTP:
			return ftpClients.length;
		case HTTP:
			return httpClients.length;
		case TCP:
			return tcpClients.length;
		case UDP:
			return udpClients.length;
		default:
			return 1;
		}
	}

}
