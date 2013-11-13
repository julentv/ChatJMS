package es.deusto.ingenieria.ssd.chat.jms.exceptions;

/**
 * 
 * @author JulenTV
 *
 */
public class IncorrectMessageException extends Exception {

	
	private static final long serialVersionUID = 1L;

	public IncorrectMessageException() {
		super("The received message in not correct");
	}

	public IncorrectMessageException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public IncorrectMessageException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public IncorrectMessageException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	public IncorrectMessageException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
		// TODO Auto-generated constructor stub
	}

}
