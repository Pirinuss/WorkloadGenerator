package wg.core;

import java.util.concurrent.Callable;

public class Event implements Callable<Response> {

	public Response call() throws Exception {
		Response response = new Response();
		response.setTest("Es klappt");
		return response;
	}

}
