package ru.ifmo.ctddev.rybak.calc;

public class InvalidExpressionCalcException extends CalcException {

	protected static String createStringPointer(int pos) {
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < pos; i++) {
			s.append(' ');
		}
		s.append('^');
		return s.toString();
	}

	public InvalidExpressionCalcException() {
		super();
	}

	public InvalidExpressionCalcException(String message) {
		super(message + "\n");
	}

	public InvalidExpressionCalcException(Exception cause) {
		super(cause);
	}

	public InvalidExpressionCalcException(String message, Exception cause) {
		super(message, cause);
	}

	public InvalidExpressionCalcException(String expression, String message) {
		super(expression + "\n" + message + "\n");
	}

	public InvalidExpressionCalcException(String expression, String message,
			int position) {
		super(expression + "\n" + createStringPointer(position) + "\n"
				+ message + " at position " + position + ".\n");
	}
}
