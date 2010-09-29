package org.sc.obo.annotations;

public class OBOTermValueException extends RuntimeException {

	public OBOTermValueException(String message) {
		super(message);
	}

	public OBOTermValueException(Throwable cause) {
		super(cause);
	}
}
