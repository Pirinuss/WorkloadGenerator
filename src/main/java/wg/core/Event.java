package wg.core;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
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
	private Response response;
	final String USER_AGENT = "Mozilla/5.0";
	
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
		response = new Response();
		response.setTarget(target);
		response.setRequest(request);
		switch(request.getProtocol()) {
		
		case HTTP:
			return response = executeHttpEvent();
		case FTP:
			return response;
		case TCP:
			return response;
		case UDP:
			return response;
		default:
			return response;
		}
	}
	
	private Response executeHttpEvent() {
		HttpRequest httpRequest = (HttpRequest) request;
		String serverName = "http://" + target.getServerName()+"/";
		String path = httpRequest.getResourcePath();
		String url = serverName.concat(path);
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
			System.out.println("Invalid Url " + url);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error at executeHttpGet");
		} finally {
			httpCon.disconnect();
		}
		return response;
	}

}
