package wg.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.concurrent.Callable;

import wg.requests.HttpMethodType;
import wg.requests.HttpRequest;
import wg.workload.Request;
import wg.workload.Target;

public class Event implements Callable<Response> {
	
	private Target target;
	private Request request;
	
	public Target getTarget() {
		return target;
	}

	public void setTarget(Target target) {
		this.target = target;
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public Event(Target target, Request request) {
		this.target = target;
		this.request = request;
	}

	public Response call() throws Exception {
		Response response = new Response();
		switch(request.getProtocol()) {
		
		case HTTP:
			return response = executeHttpEvent();
		case FTP:
			break;
		case TCP:
			break;
		case UDP:
			break;
		default:
			break;
		}
		return response;
	}
	
	private Response executeHttpEvent() throws IOException{
		//Initialwerte
		Response response = new Response();
		response.setTarget(target);
		response.setRequest(request);
		HttpRequest httpRequest = (HttpRequest) request;
		String serverName = "http://" + target.getServerName()+"/";
		String path = httpRequest.getResourcePath();
		String url = serverName.concat(path);
		String methodType = HttpMethodType.parseToString(httpRequest.getMethod());
		final String USER_AGENT = "Mozilla/5.0";
		
		//Aufbau und Ausführen der Verbindung
		//TODO Timestamps richt setzen
		URL obj = null;
		try {
			obj = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		Timestamp startTime = new Timestamp(System.currentTimeMillis());
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		String redirect = con.getHeaderField("Location");
		if (redirect != null){
		    con = (HttpURLConnection) new URL(redirect).openConnection();
		    
		}
		con.setRequestProperty("User-Agent", USER_AGENT);
		try {
			con.setRequestMethod(methodType);
		} catch (ProtocolException e) {
			System.out.println("Unknown protocol");
			e.printStackTrace();
		}
		Timestamp endTime = new Timestamp(System.currentTimeMillis());
		
		//Ergebnisauswertung
		StringBuilder sb = new StringBuilder();
		int responseCode = con.getResponseCode();
		sb.append("Response Code: " + responseCode);
		BufferedReader in = null;
		//StringBuffer responseString = null;
		/**try {
			in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			responseString = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				responseString.append(inputLine);
			}
			in.close();
		} catch (Exception e ) {
			return null;
		} **/
		response.setResponseInfos(sb.toString());
		//response.setResponseContent(responseString.toString());
		response.setEventStartTime(startTime.getTime());
		response.setEventStopTime(endTime.getTime());
		return response;
	}

}
