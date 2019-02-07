package wg.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import bftsmart.tom.ServiceProxy;
import wg.requests.BftsmartRequest;
import wg.requests.FtpMethodType;
import wg.requests.FtpRequest;
import wg.requests.HttpRequest;
import wg.requests.TcpRequest;
import wg.requests.UdpRequest;
import wg.responses.BftsmartResponse;
import wg.responses.FtpResponse;
import wg.responses.HttpResponseObject;
import wg.responses.TcpUdpResponse;
import wg.workload.Request;
import wg.workload.Target;
import wg.workload.options.Clients;

public class Event implements Callable<Response> {

	private static final Logger log = Logger.getLogger("logfile.txt");

	private final Target target;
	private final Target[] targetGroup;
	private final Request request;
	private final Clients clients;
	private final int clientIndex;
	private final String USER_AGENT = "Mozilla/5.0";

	public Event(Target target, Request request, Clients clients,
			int clientIndex) {
		this.target = target;
		this.request = request;
		this.clients = clients;
		this.clientIndex = clientIndex;
		this.targetGroup = null;
	}

	public Event(Target[] targetGroup, Request request, Clients clients,
			int clientIndex) {
		this.targetGroup = targetGroup;
		this.request = request;
		this.clients = clients;
		this.clientIndex = clientIndex;
		this.target = null;
	}

	public Response call() throws Exception {
		switch (request.getProtocol()) {

		case HTTP:
			return executeHttpEvent();
		case FTP:
			return executeFtpEvent();
		case TCP:
			return executeTcpEvent();
		case UDP:
			return executeUdpEvent();
		case BFTSMaRt:
			return executeBftsmartEvent();
		default:
			return null;
		}

	}

	private Response executeHttpEvent() throws WorkloadExecutionException {

		HttpRequest httpRequest = (HttpRequest) request;

		// Get Client
		HttpClient httpclient;
		if (clients.getHttpClients()[clientIndex] == null) {
			httpclient = HttpClients.createDefault();
			clients.getHttpClients()[clientIndex] = httpclient;
		} else {
			httpclient = clients.getHttpClients()[clientIndex];
		}

		// Build URI
		URI uri;
		try {
			uri = new URIBuilder().setScheme("http")
					.setHost(target.getServerName())
					.setPath("/" + httpRequest.getResourcePath()).build();
		} catch (URISyntaxException e) {
			throw new WorkloadExecutionException("Invalid URL!", e);
		}

		// Execute Request
		HttpResponse response = null;
		StringEntity content;
		try {
			content = new StringEntity(httpRequest.getContent());
		} catch (UnsupportedEncodingException e) {
			throw new WorkloadExecutionException("Invalid content!", e);
		}
		long startTime = System.currentTimeMillis();
		try {
			switch (httpRequest.getMethod()) {
			case DELETE:
				HttpDelete httpDelete = new HttpDelete(uri);
				httpDelete.setHeader("User-Agent", USER_AGENT);
				response = httpclient.execute(httpDelete);
				break;
			case GET:
				HttpGet httpGet = new HttpGet(uri);
				httpGet.setHeader("User-Agent", USER_AGENT);
				response = httpclient.execute(httpGet);
				break;
			case POST:
				HttpPost httpPost = new HttpPost(uri);
				httpPost.setHeader("User-Agent", USER_AGENT);
				httpPost.setEntity(content);
				response = httpclient.execute(httpPost);
				break;
			case PUT:
				HttpPut httpPut = new HttpPut(uri);
				httpPut.setHeader("User-Agent", USER_AGENT);
				httpPut.setEntity(content);
				response = httpclient.execute(httpPut);
				break;
			}
		} catch (IOException e) {
			throw new WorkloadExecutionException(
					"Error while executing HTTP request!", e);
		}
		long endTime = System.currentTimeMillis();
		return new HttpResponseObject(startTime, endTime, target, response);

	}

	private Response executeFtpEvent() throws WorkloadExecutionException {

		FtpRequest ftpRequest = (FtpRequest) request;

		// Get Client
		FTPClient ftpClient;
		if (clients.getFtpClients()[clientIndex] == null) {
			ftpClient = new FTPClient();
			clients.getFtpClients()[clientIndex] = ftpClient;
		} else {
			ftpClient = clients.getFtpClients()[clientIndex];
		}

		// Execute Request
		long startTime = System.currentTimeMillis();
		int replyCode = 0;
		try {
			// TODO FTPConnectionClosedException abfangen (passiert wenn FTP
			// response = 421)
			ftpClient.connect(target.getServerName(), target.getPort());
			ftpClient.login(ftpRequest.getUsername(), ftpRequest.getPassword());
			if (ftpRequest.getMethod() == FtpMethodType.GET) {
				FileOutputStream fos = new FileOutputStream(
						ftpRequest.getLocalResource());
				ftpClient.retrieveFile(ftpRequest.getRemoteResource(), fos);
				replyCode = ftpClient.getReplyCode();
				fos.close();
			}
			if (ftpRequest.getMethod() == FtpMethodType.PUT) {
				FileInputStream fis = new FileInputStream(
						ftpRequest.getLocalResource());
				ftpClient.storeFile(ftpRequest.getRemoteResource(), fis);
				replyCode = ftpClient.getReplyCode();
				fis.close();
			}
			ftpClient.logout();
			ftpClient.disconnect();
		} catch (IOException e) {
			throw new WorkloadExecutionException(
					"Error while executing FTP request!", e);
		}
		long endTime = System.currentTimeMillis();
		return new FtpResponse(startTime, endTime, target, replyCode);
	}

	private Response executeTcpEvent() throws WorkloadExecutionException {
		TcpRequest tcpRequest = (TcpRequest) request;
		String responseContent = null;
		long startTime = System.currentTimeMillis();
		try {
			Socket socket;
			if (clients.getTcpClients()[clientIndex] == null) {
				socket = new Socket(target.getServerName(), target.getPort());
				clients.getTcpClients()[clientIndex] = socket;
			} else {
				socket = clients.getTcpClients()[clientIndex];
			}

			socket.setSoTimeout(5000);
			DataOutputStream outToServer = new DataOutputStream(
					socket.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			outToServer.writeBytes(tcpRequest.getContent());
			responseContent = inFromServer.readLine();
			socket.close();
		} catch (SocketTimeoutException e) {
			throw new WorkloadExecutionException("TCP connection timed out!",
					e);
		} catch (ConnectException e) {
			throw new WorkloadExecutionException(
					"Could not estabilish TCP connection!", e);
		} catch (IOException e) {
			throw new WorkloadExecutionException(
					"Error while executing TCP request!", e);
		}
		long endTime = System.currentTimeMillis();
		return new TcpUdpResponse(startTime, endTime, target, responseContent);
	}

	private Response executeUdpEvent() throws WorkloadExecutionException {
		UdpRequest udpRequest = (UdpRequest) request;
		String responseContent = null;
		long startTime = 0;
		long endTime = 0;
		try {
			DatagramSocket clientSocket;
			if (clients.getUdpClients()[clientIndex] == null) {
				clientSocket = new DatagramSocket();
				clients.getUdpClients()[clientIndex] = clientSocket;
			} else {
				clientSocket = clients.getUdpClients()[clientIndex];
			}

			clientSocket.setSoTimeout(30000);
			InetAddress IPAddress = InetAddress
					.getByName(target.getServerName());
			byte[] sendData = new byte[1024];
			byte[] receiveData = new byte[1024];
			String sentence = udpRequest.getContent();
			sendData = sentence.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, IPAddress, target.getPort());
			startTime = System.currentTimeMillis();
			clientSocket.send(sendPacket);
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			clientSocket.receive(receivePacket);
			endTime = System.currentTimeMillis();
			responseContent = new String(receivePacket.getData());
			clientSocket.close();
		} catch (SocketTimeoutException e) {
			System.out.println(
					"UDP Verbindung kann nicht aufgebaut werden. Grund: Verbindung ist getimeouted.");
		} catch (IOException e) {
			throw new WorkloadExecutionException(
					"Error while executing UDP request!", e);
		}
		return new TcpUdpResponse(startTime, endTime, target, responseContent);
	}

	private Response executeBftsmartEvent() throws WorkloadExecutionException {

		/*
		 * "command" : { "type": "ByteObjectStream", "content":
		 * "01:int,02:long,03:String", "01": 42, "03": "eiaoaer" "02": 12497914
		 * }
		 * 
		 * 
		 * 
		 * "command" : { "type": "byteArray", "content": "ANEHOSB147eE==/" }
		 */

		BftsmartRequest bftRequest = (BftsmartRequest) request;
		// Set hosts
		try {
			FileWriter fw = new FileWriter("config/hosts.config");
			BufferedWriter bw = new BufferedWriter(fw);
			for (int j = 0; j < targetGroup.length; j++) {
				int port = Integer.valueOf(targetGroup[j].getPort());
				String serverName = target.getServerName();
				InetAddress ip;
				ip = InetAddress.getByName(serverName);
				bw.write(j + " ");
				bw.write(ip.getHostAddress() + " ");
				bw.write(String.valueOf(port));
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// BFTSMaRt Client
		char id = target.getTargetID()
				.charAt(target.getTargetID().length() - 1);
		byte[] command = bftRequest.getCommand().getBytes();

		byte[] reply = null;
		long startTime = 0;
		long endTime = 0;
		try {
			ServiceProxy serviceProxy;
			if (clients.getBftsmartClients()[clientIndex] == null) {
				serviceProxy = new ServiceProxy(id);
				clients.getBftsmartClients()[clientIndex] = serviceProxy;
			} else {
				serviceProxy = clients.getBftsmartClients()[clientIndex];
			}
			ByteArrayOutputStream out = new ByteArrayOutputStream(4);
			new DataOutputStream(out).writeInt(1);
			startTime = System.currentTimeMillis();
			if (bftRequest.getType().equals("ordered")) {
				reply = serviceProxy.invokeOrdered(command);
			} else {
				reply = serviceProxy.invokeUnordered(command);
			}
			endTime = System.currentTimeMillis();
			if (reply != null) {
				int newValue = new DataInputStream(
						new ByteArrayInputStream(reply)).readInt();
				log.info("Returned value: " + newValue);
			} else {
				log.severe("Error while executing BFTSMaRt request");
			}
		} catch (IOException e) {
			if (e.getMessage().equals("Impossible to connect to servers!")) {
				throw new WorkloadExecutionException(
						"Error while executing BFTSMaRt request! Could not connect to servers!",
						e);
			} else {
				throw new WorkloadExecutionException(
						"Error while executing BFTSMaRt request!", e);
			}
		}
		return new BftsmartResponse(startTime, endTime, target, reply);
	}

}
