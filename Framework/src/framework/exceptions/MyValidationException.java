package framework.exceptions;

public class MyValidationException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MyValidationException(String errorMessage) {
        super(errorMessage);
    }
}
