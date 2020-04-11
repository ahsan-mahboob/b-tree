package org.apache.dts.btree;

/**
 * Class BTException.
 */
public class BTException extends Exception {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -1913860689978477184L;

	/**
	 * Instantiates a new BT exception.
	 */
	public BTException() {
		super();
	}

	/**
	 * Instantiates a new BT exception.
	 *
	 * @param message the message
	 */
	public BTException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new BT exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public BTException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new BT exception.
	 *
	 * @param cause the cause
	 */
	public BTException(Throwable cause) {
		super(cause);
	}
}
