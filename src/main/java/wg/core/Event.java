package wg.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.apache.commons.net.ftp.FTPClient;

import bftsmart.tom.ServiceProxy;
import wg.requests.BftsmartRequest;
import wg.requests.FtpMethodType;
import wg.requests.FtpRequest;
import wg.requests.HttpMethodType;
import wg.requests.HttpRequest;
import wg.requests.TcpUdpRequest;
import wg.workload.Request;
import wg.workload.Target;
import wg.workload.Workload;

public class Event implements Callable<Response> {
	
	private final Workload workload;
	private final Target target;
	private final Request request;
	private Response response;
	private final String USER_AGENT = "Mozilla/5.0";
	
	public Event(Target target, Request request, Workload workload) {
		this.target = target;
		this.request = request;
		this.workload = workload;
	}
	
	public Target getTarget() {
		return target;
	}

	public Request getRequest() {
		return request;
	}

	public Response call() throws Exception {
		response = new Response();
		switch(request.getProtocol()) {
		
		case HTTP:
			response = executeHttpEvent();
			break;
		case FTP:
			response = executeFtpEvent();
			break;
		case TCP:
			response = executeTcpEvent();
			break;
		case UDP:
			response = executeUdpEvent();
			break;
		case BFTSMaRt:
			response = executeBftsmartEvent();
			break;
		default:
			response = null;
		}
		
		response.setTarget(target);
		response.setRequest(request);
		return response;
	}
	
	private Response executeHttpEvent() {
		HttpRequest httpRequest = (HttpRequest) request;
		String serverName = "http://" + target.getServerName()+"/";
		String path = httpRequest.getResourcePath();
		String url;
		if (path == null) {
			url = serverName;
		} else {
			url = serverName.concat(path);
		}
		String methodType = HttpMethodType.parseToString(httpRequest.getMethod());
		String content = httpRequest.getContent();
		Timestamp startTime = new Timestamp(System.currentTimeMillis());
		response = executeHttpRequest(url, methodType, content);
		Timestamp endTime = new Timestamp(System.currentTimeMillis());
		response.setEventStartTime(startTime.getTime());
		response.setEventStopTime(endTime.getTime());
		return response;
	}
	
	private Response executeHttpRequest(String url, String methodType, String content) {
		HttpURLConnection httpCon = null;
		try {
			URL urlObj = new URL(url);
			httpCon = (HttpURLConnection) urlObj.openConnection();
			httpCon.setDoOutput(true);
			httpCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded" );
			httpCon.setRequestProperty("User-Agent", USER_AGENT);
			httpCon.setRequestMethod(methodType);
			httpCon.setConnectTimeout(3000);
			//TODO SocketTimeoutException abfangen
			httpCon.connect();
			if (methodType.equals("PUT") || methodType.equals("POST")) {
				OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
				out.write(content);
				out.flush();
				out.close();
			}
			String redirect = httpCon.getHeaderField("Location");
			if (redirect != null){
			    httpCon = (HttpURLConnection) new URL(redirect).openConnection();  
			}
			int responseCode = httpCon.getResponseCode();
			if (responseCode == 200) {
				BufferedReader in = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
				String inputLine;
				StringBuffer responseContent = new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
					responseContent.append(inputLine);
				}
				in.close();
				response.setResponseContent(responseContent.toString());
			}
			response.setResponseInfos(String.valueOf(responseCode));
		} catch (FileNotFoundException e) {
			response.setResponseInfos(String.valueOf(404));
			System.out.println("Invalid path: " + url);
		} catch (UnknownHostException e) {
			response.setResponseInfos(String.valueOf(404));
			System.out.println("Invalid server name: " + url);
		} catch (Exception e) {
			System.out.println("Error at executeHttpGet");
			e.printStackTrace();
		} finally {
			httpCon.disconnect();
		}
		return response;
	}
	
	private Response executeFtpEvent() {
		FtpRequest ftpRequest = (FtpRequest) request;
		String serverName = target.getServerName();
		int port = Integer.valueOf(target.getPort());
		String username = ftpRequest.getUsername();
		String password = ftpRequest.getPassword();
		FtpMethodType method = ftpRequest.getMethod();
		String localResource = ftpRequest.getLocalResource();
		String remoteResource = ftpRequest.getRemoteResource();
		FTPClient ftpClient = new FTPClient();
		Timestamp startTime = new Timestamp(System.currentTimeMillis());
		try {
			//TODO FTPConnectionClosedException abfangen (passiert wenn FTP response = 421)
			ftpClient.connect(serverName, port);
			ftpClient.login(username, password);
			if (method == FtpMethodType.GET) {
				FileOutputStream fos = new FileOutputStream(localResource);
				ftpClient.retrieveFile(remoteResource, fos);
				response.setResponseInfos(String.valueOf(ftpClient.getReplyCode()));
				fos.close();
			} 
			if (method == FtpMethodType.PUT) {
				FileInputStream fis = new FileInputStream(localResource);
				ftpClient.storeFile(remoteResource, fis);
				response.setResponseInfos(String.valueOf(ftpClient.getReplyCode()));
				fis.close();
			}
			ftpClient.logout();
			ftpClient.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Timestamp endTime = new Timestamp(System.currentTimeMillis());
		response.setEventStartTime(startTime.getTime());
		response.setEventStopTime(endTime.getTime());
		return response;
	}
	
	private Response executeTcpEvent() {
		TcpUdpRequest tcpRequest = (TcpUdpRequest) request;
		String serverName = target.getServerName();
		int port = Integer.valueOf(target.getPort());
		String responseContent = null;
		Timestamp startTime = new Timestamp(System.currentTimeMillis());
		try {
			Socket socket = new Socket(serverName, port);
			socket.setSoTimeout(5000);
			DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outToServer.writeBytes(tcpRequest.getContent());
			responseContent = inFromServer.readLine();
			socket.close();
		} catch (SocketTimeoutException e) {
			System.out.println("TCP Verbindung kann nicht aufgebaut werden. Grund: Verbindung ist getimeouted.");
		} catch (ConnectException e) {
			System.out.println("TCP Verbindung kann nicht aufgebaut werden. Grund: Verbindung konnte nicht aufgebaut werden.");
		} catch (Exception e) {
			e.printStackTrace();
		}
		Timestamp endTime = new Timestamp(System.currentTimeMillis());
		response.setEventStartTime(startTime.getTime());
		response.setEventStopTime(endTime.getTime());
		response.setResponseContent(responseContent);
		return response;
	}
	
	private Response executeUdpEvent() {
		TcpUdpRequest udpRequest = (TcpUdpRequest) request;
		String serverName = target.getServerName();
		int port = Integer.valueOf(target.getPort());
		String responseContent = null;
		Timestamp startTime = null;
		Timestamp endTime = null;
		try {
			DatagramSocket clientSocket = new DatagramSocket();
			clientSocket.setSoTimeout(30000);
			InetAddress IPAddress = InetAddress.getByName(serverName);
			byte[] sendData = new byte[1024];
			byte[] receiveData = new byte[1024];
			String sentence = udpRequest.getContent();
			sendData = sentence.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			startTime = new Timestamp(System.currentTimeMillis());
			clientSocket.send(sendPacket);
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			clientSocket.receive(receivePacket);
			endTime = new Timestamp(System.currentTimeMillis());
			responseContent = new String(receivePacket.getData());
			clientSocket.close();
		} catch (SocketTimeoutException e) {
			System.out.println("UDP Verbindung kann nicht aufgebaut werden. Grund: Verbindung ist getimeouted.");
		} catch (Exception e) {
			e.printStackTrace();
		}
		response.setResponseContent(responseContent);
		response.setEventStartTime(startTime.getTime());
		response.setEventStopTime(endTime.getTime());
		return response;
	}
	
	private Response executeBftsmartEvent() {
		BftsmartRequest bftRequest = (BftsmartRequest) request;
		//Set hosts
		ArrayList<Target> targetGroup = new ArrayList<Target>();
		ArrayList<String> targetNamesGroup = bftRequest.getTargetGroup();
		for (int i=0; i<targetNamesGroup.size(); i++) {
			Target target;
			target = workload.getTargetByName(targetNamesGroup.get(i));
			targetGroup.add(target);
		}
		try {
			FileWriter fw = new FileWriter("config/hosts.config");
			BufferedWriter bw = new BufferedWriter(fw);
			for (int j=0; j<targetGroup.size(); j++) {
				int port = Integer.valueOf(targetGroup.get(j).getPort());
				String serverName = target.getServerName();
				InetAddress ip;
				ip = InetAddress.getByName(serverName);
				bw.write(j + " ");
				bw.write(ip.getHostAddress()+ " ");
				bw.write(String.valueOf(port));
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//BFTSMaRt Client
		char id = target.getTargetName().charAt(target.getTargetName().length()-1);
		byte [] command = bftRequest.getCommand().getBytes();
		
		byte[] reply = null;
        try {
        	ByteArrayOutputStream out = new ByteArrayOutputStream(4);
			new DataOutputStream(out).writeInt(1);
			ServiceProxy serviceProxy = new ServiceProxy(0 , "config");
			reply = serviceProxy.invokeOrdered(out.toByteArray());
			int newValue = new DataInputStream(new ByteArrayInputStream(reply)).readInt();
            System.out.println(", returned value: " + newValue);
		} catch (IOException e) {
			e.printStackTrace();
		}
        response.setResponseContent(reply.toString());
		return response;
	}

}
