package ru.ifmo.ctddev.rybak.calc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CalcTree {

	private final ExpressionNode root;
	private int currentPos;
	private final String expression;
	private Set<Character> vars;

	public static final double MAX = 1e10;
	public static final double MIN = -MAX;

	public CalcTree(String expression, Set<Character> vars)
			throws InvalidExpressionCalcException {
		this.expression = expression.trim();
		if (this.expression.length() == 0) {
			throw new EmptyExpressionCalcException();
		}
		this.vars = vars;
		currentPos = 0;
		root = new ExpressionNode();
		checkTrashAtEndOfExpression();
	}

	private void checkTrashAtEndOfExpression()
			throws NotAllowedCharacterCalcException {
		if (currentPos != expression.length()) {
			throw new NotAllowedCharacterCalcException(this.expression,
					"dirt in the end of string.", currentPos);
		}
	}

	public String getExpression() {
		return this.expression;
	}

	public double calculate(Map<Character, Double> vars)
			throws MathCalcException {
		return root.calculate(vars);
	}

	private boolean checkOverflow(double a) {
		return MIN <= a && a <= MAX;
	}

	private void checkOverflowSimple(double a) throws OverflowCalcException {
		if (!checkOverflow(a)) {
			throw new OverflowCalcException();
		}
	}

	@SuppressWarnings("unused")
	private void checkOverflowMulti(double a, String function, int position,
			double[] operands) throws OverflowCalcException {
		if (!checkOverflow(a)) {
			StringBuffer message = new StringBuffer(function);
			message.append('(');
			for (int i = 0; i < operands.length; ++i) {
				message.append(operands[i]);
				if (i < operands.length - 1) {
					message.append(", ");
				}
			}
			message.append(") = ");
			message.append(a);
			throw new OverflowCalcException(message.toString());
		}
	}

	@SuppressWarnings("unused")
	private void checkOverflowBinary(double a, String operation, int position,
			double operand1, double operand2) throws OverflowCalcException {
		if (!checkOverflow(a)) {
			StringBuffer message = new StringBuffer();
			message.append(operand1);
			message.append(' ');
			message.append(operation);
			message.append(' ');
			message.append(operand2);
			message.append(" = ");
			message.append(a);
			throw new OverflowCalcException(message.toString(), position);
		}
	}

	@SuppressWarnings("unused")
	private void checkOverflowUnary(double a, String message, int position)
			throws OverflowCalcException {
		if (!checkOverflow(a)) {
			StringBuffer sb = new StringBuffer();
			sb.append(message);
			sb.append(' ');
			sb.append(a);
			throw new OverflowCalcException(sb.toString(), position);
		}
	}

	private abstract class CalcTreeNode {
		abstract double calculate(Map<Character, Double> vars)
				throws MathCalcException;
	}

	private class ExpressionNode extends CalcTreeNode {
		private List<FactorNode> nodes;

		public ExpressionNode() throws InvalidExpressionCalcException {
			nodes = new ArrayList<CalcTree.FactorNode>();
			nodes.add(new FactorNode(false));
			skipWhiteSpace();
			while (currentPos < expression.length()) {
				char c = getNextChar();
				switch (c) {
				case '+':
					nodes.add(new FactorNode(false));
					break;
				case '-':
					nodes.add(new FactorNode(true));
					break;
				case ')':
					pushBack();
					return;
				default:
					throw new WasExpectedCalcException(expression,
							"'+' or '-' or ')'", currentPos - 1);
				}
				skipWhiteSpace();
			}
		}

		public double calculate(Map<Character, Double> vars)
				throws MathCalcException {
			double res = nodes.get(0).calculate(vars);
			for (int i = 1, n = nodes.size(); i < n; i++) {
				FactorNode node = nodes.get(i);
				double temp = node.calculate(vars);
				// String operation;
				if (node.isNegative()) {
					// operation = "-";
					temp = -temp;
				} else {
					// operation = "+";
				}
				checkOverflowSimple(res + temp);
				// checkOverflowBinary(res + temp, operation, node.position - 1,
				// res, temp);
				res += temp;
			}
			return res;
		}
	}

	private class FactorNode extends CalcTreeNode {

		final static double EPS = 1e-10;
		private boolean negative;
		private List<ParenthesesNode> nodes;

		public FactorNode(boolean negative)
				throws InvalidExpressionCalcException {
			this.negative = negative;
			nodes = new ArrayList<CalcTree.ParenthesesNode>();
			nodes.add(new ParenthesesNode(false));
			skipWhiteSpace();
			while (currentPos < expression.length()) {
				char c = getNextChar();
				switch (c) {
				case '*':
					nodes.add(new ParenthesesNode(false));
					break;
				case '/':
					nodes.add(new ParenthesesNode(true));
					break;
				default:
					if (c == ')' || c == '+' || c == '-') {
						pushBack();
						return;
					} else {
						throw new NotAllowedCharacterCalcException(expression,
								currentPos - 1);
					}
				}
				skipWhiteSpace();
			}
		}

		private void checkForDivision(double divisor) throws DBZCalcException {
			if (Math.abs(divisor) <= EPS) {
				throw new DBZCalcException();
			}
		}

		@Override
		public double calculate(Map<Character, Double> vars)
				throws MathCalcException {
			double res = 1.0;
			for (ParenthesesNode node : nodes) {
				// double a = res;
				double b = node.calculate(vars);
				if (node.isDivision()) {
					checkForDivision(b);
					res /= b;
					// checkOverflowBinary(res, "/", node.position - 1, a, b);
				} else {
					res *= b;
					// checkOverflowBinary(res, "*", node.position - 1, a, b);
				}
				checkOverflowSimple(res);
			}
			return res;
		}

		public boolean isNegative() {
			return negative;
		}

	}

	private class ParenthesesNode extends CalcTreeNode {
		private boolean division;
		private boolean negative;
		private CalcTreeNode node;

		public ParenthesesNode(boolean division)
				throws InvalidExpressionCalcException {
			this.division = division;
			this.negative = false;
			skipWhiteSpace();
			processUnarySigns();
			if (currentPos < expression.length()) {
				if (getNextChar() == '(') {
					processParentheses();
				} else {
					pushBack();
					node = new NumberNode();
				}
			} else {
				throw new WasExpectedCalcException(expression,
						"Number or variable or '(' ", expression.length());
			}
		}

		private void processUnarySigns()
				throws NotAllowedCharacterCalcException {
			if (currentPos < expression.length()) {
				char c = getNextChar();
				while (c == '+' || c == '-') {
					if (c == '-') {
						negative = !negative;
					}
					if (currentPos < expression.length()) {
						c = getNextChar();
					} else {
						return;
					}
				}
				pushBack();
			}
		}

		private void processParentheses() throws InvalidExpressionCalcException {
			skipWhiteSpace();
			node = new ExpressionNode();
			skipWhiteSpace();
			if (currentPos >= expression.length() || getNextChar() != ')') {
				throw new ParenthesesWasExpectedCalcException(expression,
						currentPos >= expression.length() ? expression.length()
								: currentPos - 1);
			}
		}

		@Override
		public double calculate(Map<Character, Double> vars)
				throws MathCalcException {
			double res = node.calculate(vars);
			return negative ? -res : res;
		}

		public boolean isDivision() {
			return division;
		}
	}

	private static final Pattern unsignedDoublePattern = Pattern
			.compile("[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?");

	private class NumberNode extends CalcTreeNode {
		private static final String EXCEPTION_MESSAGE = "Number or variable";
		private double number;
		private char var;
		private boolean isVar;

		public NumberNode() throws InvalidExpressionCalcException {
			skipWhiteSpace();
			this.isVar = false;
			if (currentPos < expression.length()) {
				char c = getNextChar();
				if (vars.contains(c)) {
					processVariable(c);
				} else {
					if (Character.isDigit(c)) {
						pushBack();
						processDouble();
					} else {
						throw new WasExpectedCalcException(expression,
								EXCEPTION_MESSAGE, currentPos - 1);
					}
				}
			} else {
				throw new WasExpectedCalcException(expression,
						EXCEPTION_MESSAGE, expression.length());
			}
		}

		private void processVariable(char var) {
			this.isVar = true;
			this.var = var;
			this.number = 0;
		}

		private void processDouble() throws WrongNumberFormatCalcException {
			Matcher matcher = unsignedDoublePattern.matcher(expression
					.substring(currentPos));
			if (matcher.find()) {
				number = Double.parseDouble(matcher.group());
				currentPos += matcher.end();
			} else {
				throw new WrongNumberFormatCalcException(expression, currentPos);
			}
			this.var = 0;
		}

		@Override
		public double calculate(Map<Character, Double> vars)
				throws OverflowCalcException {
			double res;
			if (this.isVar) {
				if (!vars.containsKey(this.var)) {
					throw new NotDefinedVariableException(this.var);
				}
				res = vars.get(this.var);
			} else {
				res = number;
			}
			// checkOverflowUnary(res, this.isX ? "x = " : "number",
			// this.position);
			checkOverflowSimple(res);
			return res;
		}

	}

	private void skipWhiteSpace() {
		while (currentPos < expression.length()
				&& Character.isWhitespace(expression.charAt(currentPos))) {
			currentPos++;
		}
	}

	private char getNextChar() throws NotAllowedCharacterCalcException {
		char c = expression.charAt(currentPos);
		if (!isAllowedSymbol(c)) {
			throw new NotAllowedCharacterCalcException(expression, currentPos);
		}
		currentPos++;
		return c;
	}

	private void pushBack() {
		currentPos--;
	}

	private boolean isAllowedSymbol(char c) {
		return Character.isWhitespace(c) || isDoubleSymbol(c)
				|| isOperatorSymbol(c) || vars.contains(c) || c == '('
				|| c == ')';
	}

	private boolean isOperatorSymbol(char c) {
		return c == '+' || c == '-' || c == '*' || c == '/';
	}

	private boolean isDoubleSymbol(char c) {
		return Character.isDigit(c) || c == '.' || c == 'E' || c == 'e'
				|| c == '-';
	}

}