package wg.parser;

import wg.WorkloadGeneratorException;

public class WorkloadParserException extends WorkloadGeneratorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WorkloadParserException(String message, Throwable cause) {
		super(message, cause);
	}

	public WorkloadParserException(String message) {
		super(message);
	}

}
