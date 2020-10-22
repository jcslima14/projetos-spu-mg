package framework;

public class MyException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MyException(String errorMessage) {
        super(errorMessage);
    }
}
