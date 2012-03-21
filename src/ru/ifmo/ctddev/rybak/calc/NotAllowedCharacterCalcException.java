package ru.ifmo.ctddev.rybak.calc;

public class NotAllowedCharacterCalcException extends
		InvalidExpressionCalcException {

	public NotAllowedCharacterCalcException() {
		super();
	}

	public NotAllowedCharacterCalcException(String message) {
		super(message);
	}

	public NotAllowedCharacterCalcException(Exception cause) {
		super(cause);
	}

	public NotAllowedCharacterCalcException(String message, Exception cause) {
		super(message, cause);
	}

	public NotAllowedCharacterCalcException(String expression, int position) {
		super(expression, createStringPointer(position) + "\nNot allowed character '"
				+ expression.charAt(position) + "' at position "
				+ position + ".");
	}

	public NotAllowedCharacterCalcException(String expression, String message,
			int position) {
		super(expression, createStringPointer(position) + "\nCharacter '"
				+ expression.charAt(position) + "' is not allowed at position "
				+ position + ": " + message);
	}
}