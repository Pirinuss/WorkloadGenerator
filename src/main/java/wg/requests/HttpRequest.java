package wg.requests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import wg.core.Response;
import wg.core.WorkloadExecutionException;
import wg.responses.HttpResponseObject;
import wg.workload.ProtocolType;
import wg.workload.Request;
import wg.workload.Target;

public class HttpRequest extends Request implements RequestInterface {

	private static final String USER_AGENT = "Mozilla/5.0";
	/**
	 * Maximal number of parallel connections for a HTTP client
	 */
	private static final int MAX_CONNECTIONS_PER_HTTPCLIENT = 100;
	private final HttpMethodType method;
	private final String resourcePath;
	private final String content;
	private final CloseableHttpClient[] clients;

	public HttpRequest(String requestName, ProtocolType protocol,
			long numberOfClients, HttpMethodType method, String resourcePath,
			String content) {

		super(requestName, protocol, numberOfClients);

		if (method == null) {
			throw new IllegalArgumentException("Method must not be null!");
		}
		this.method = method;

		if (resourcePath == null) {
			resourcePath = "";
		}
		this.resourcePath = resourcePath;

		if (content == null) {
			content = "";
		}
		this.content = content;

		this.clients = new CloseableHttpClient[(int) numberOfClients];
		for (int i = 0; i < numberOfClients; i++) {
			PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
			cm.setMaxTotal(MAX_CONNECTIONS_PER_HTTPCLIENT);
			clients[i] = HttpClients.custom().setConnectionManager(cm).build();
		}

	}

	@Override
	public Response[] execute(Target[] targets)
			throws WorkloadExecutionException {

		Response[] responses = new Response[clients.length * targets.length];

		int index = 0;
		for (int i = 0; i < clients.length; i++) {
			for (int j = 0; j < targets.length; j++) {
				responses[index] = executeSingleRequest(clients[i], targets[j]);
				index++;
			}
		}

		return responses;
	}

	private Response executeSingleRequest(CloseableHttpClient client,
			Target target) throws WorkloadExecutionException {

		URI uri = getUri(target);

		HttpResponse response = null;
		StringEntity content;
		try {
			content = new StringEntity(this.content);
		} catch (UnsupportedEncodingException e) {
			throw new WorkloadExecutionException("Invalid content!", e);
		}
		long startTime = System.currentTimeMillis();
		try {
			switch (method) {
			case DELETE:
				HttpDelete httpDelete = new HttpDelete(uri);
				httpDelete.setHeader("User-Agent", USER_AGENT);
				response = client.execute(httpDelete);
				httpDelete.releaseConnection();
				break;
			case GET:
				HttpGet httpGet = new HttpGet(uri);
				httpGet.setHeader("User-Agent", USER_AGENT);
				response = client.execute(httpGet);
				httpGet.releaseConnection();
				break;
			case POST:
				HttpPost httpPost = new HttpPost(uri);
				httpPost.setHeader("User-Agent", USER_AGENT);
				httpPost.setEntity(content);
				response = client.execute(httpPost);
				httpPost.releaseConnection();
				break;
			case PUT:
				HttpPut httpPut = new HttpPut(uri);
				httpPut.setHeader("User-Agent", USER_AGENT);
				httpPut.setEntity(content);
				response = client.execute(httpPut);
				httpPut.releaseConnection();
				break;
			}
		} catch (IOException e) {
			throw new WorkloadExecutionException(
					"Error while executing HTTP request!", e);
		}
		long endTime = System.currentTimeMillis();
		return new HttpResponseObject(startTime, endTime, target, response);
	}

	private URI getUri(Target target) throws WorkloadExecutionException {
		URI uri;
		try {
			uri = new URIBuilder().setScheme("http")
					.setHost(target.getServerName()).setPath("/" + resourcePath)
					.build();
		} catch (URISyntaxException e) {
			throw new WorkloadExecutionException("Invalid URL!", e);
		}
		return uri;
	}

}
