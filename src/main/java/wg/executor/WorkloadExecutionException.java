package wg.executor;

public class WorkloadExecutionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public WorkloadExecutionException(String message, Throwable cause) {
		super(message, cause);
	}

	public WorkloadExecutionException(String message) {
		super(message);
	}

}
