package wg.requests;

public enum HttpMethodType {
	
	GET, PUT, POST, DELETE, NONE;
	
	public static String parseToString(HttpMethodType methodType) {
		switch (methodType) {
		
		case DELETE:
			return "DELETE";
		case GET:
			return "GET";
		case POST:
			return "POST";
		case PUT:
			return "PUT";
		default:
			return null;
		
		}
	}

}
