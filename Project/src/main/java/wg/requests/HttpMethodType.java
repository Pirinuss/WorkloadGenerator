package wg.requests;

public enum HttpMethodType {

	GET, PUT, POST, DELETE;

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

	public static HttpMethodType fromString(String identifier) {
		switch (identifier.toUpperCase()) {
		case "GET":
			return GET;
		case "DELETE":
			return DELETE;
		case "POST":
			return POST;
		case "PUT":
			return PUT;
		default:
			throw new IllegalArgumentException(
					"Unknown identifier! " + identifier);
		}
	}

}
