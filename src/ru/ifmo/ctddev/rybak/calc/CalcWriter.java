package ru.ifmo.ctddev.rybak.calc;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalcWriter {

	private static final int START = 0;
	private static final int FINISH = 10;
	private static final int AMOUNT = FINISH - START + 1;
	private final static String TABULATION = "   ";

	private List<CalcTree> calcTrees;
	private List<String> messages;
	private String[][] outputTable;
	private Map<Character, Double> varsValues;
	private int[] width;

	private final int COLUMNS;
	private final int ROWS;

	public CalcWriter(final String expression) {
		this(new ArrayList<String>() {
			{
				add(expression);
			}
		});
	}

	public CalcWriter(final List<String> expressions) {
		this.messages = new ArrayList<String>();
		this.varsValues = createVars();
		this.calcTrees = new ArrayList<CalcTree>();
		int lineNumber = 0;
		for (String expression : expressions) {
			lineNumber++;
			processExpression(expression, lineNumber);
		}
		ROWS = calculateRows();
		COLUMNS = calculateColumns();
		generateOutputTable();
	}

	private Map<Character, Double> createVars() {
		Map<Character, Double> varsValues = new HashMap<Character, Double>();
		varsValues.put('x', 0.0);
		varsValues.put('y', 0.0);
		varsValues.put('z', 0.0);
		return varsValues;
	}

	private void processExpression(String expression, int lineNumber) {
		try {
			this.calcTrees.add(new CalcTree(expression, this.varsValues
					.keySet()));
		} catch (InvalidExpressionCalcException e) {
			messages.add("Line #" + lineNumber + ":\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	private int calculateRows() {
		int res = 1;
		for (int i = 0; i < varsValues.size(); ++i) {
			res *= AMOUNT;
		}
		res += 1;
		return res;
	}

	private int calculateColumns() {
		return varsValues.size() + calcTrees.size();
	}

	private void generateOutputTable() {
		width = new int[COLUMNS];
		this.outputTable = new String[ROWS][COLUMNS];
		generateTableHeader();
		for (int x = START, row = 0; x <= FINISH; x++) {
			for (int y = START; y <= FINISH; y++) {
				for (int z = START; z <= FINISH; z++) {
					row++;
					assert row == (x - START) * (AMOUNT * AMOUNT) + (y - START)
							* (AMOUNT) + (z - START) + 1 : "Row number assertion";
					generateTableRow(row, x, y, z);
				}
			}
		}
	}

	private void generateTableHeader() {
		outputTable[0][0] = "x";
		outputTable[0][1] = "y";
		outputTable[0][2] = "z";
		relaxColumnsWidthsByRow(0, 0, varsValues.size());
		for (int j = varsValues.size(); j < COLUMNS; j++) {
			outputTable[0][j] = "f" + (j - varsValues.size() + 1);
			relaxColumnWidthByCell(0, j);
		}
	}

	private void generateTableRow(int row, int x, int y, int z) {
		outputTable[row][0] = Integer.toString(x);
		outputTable[row][1] = Integer.toString(y);
		outputTable[row][2] = Integer.toString(z);
		varsValues.put('x', (double) x);
		varsValues.put('y', (double) y);
		varsValues.put('z', (double) z);
		relaxColumnsWidthsByRow(row, 0, varsValues.size());
		for (int j = varsValues.size(); j < COLUMNS; j++) {
			generateTableCell(row, j);
			relaxColumnWidthByCell(row, j);
		}
	}

	private void relaxColumnsWidthsByRow(int row, int from, int to) {
		for (int j = from; j < to; ++j) {
			relaxColumnWidthByCell(row, j);
		}
	}

	private void relaxColumnWidthByCell(int row, int column) {
		if (width[column] < outputTable[row][column].length()) {
			width[column] = outputTable[row][column].length();
		}
	}

	private void generateTableCell(int row, int column) {
		try {
			outputTable[row][column] = Double.toString(calcTrees.get(
					column - varsValues.size()).calculate(varsValues));
		} catch (MathCalcException e) {
			outputTable[row][column] = e.getMessage();
		}
	}

	public void writeToFileWriter(FileWriter out) throws IOException {
		writeMessagesToFileWriter(out);
		writeGoodExpressionsToFileWriter(out);
		writeFormatedOutputTableToFileWriter(out);
	}

	private void writeMessagesToFileWriter(FileWriter out) throws IOException {
		for (String msg : messages) {
			out.write(msg);
		}
		if (messages.size() > 0) {
			out.write('\n');
		}
	}

	private void writeGoodExpressionsToFileWriter(FileWriter out)
			throws IOException {
		for (int i = 0; i < COLUMNS - varsValues.size(); i++) {
			out.write("f" + Integer.toString(i + 1) + " = "
					+ calcTrees.get(i).getExpression() + "\n");
		}
	}

	private void writeFormatedOutputTableToFileWriter(FileWriter out)
			throws IOException {
		StringBuffer[] formatedOutput = createFormatedOutput();
		for (int i = 0; i < formatedOutput.length; i++) {
			out.write(formatedOutput[i].toString());
			out.write('\n');
		}
	}

	private StringBuffer[] createFormatedOutput() {
		StringBuffer[] formatedOutput = new StringBuffer[ROWS];
		for (int i = 0; i < ROWS; i++) {
			formatedOutput[i] = new StringBuffer();
			for (int j = 0; j < COLUMNS; j++) {
				appendSpacesToStringBuffer(formatedOutput[i], width[j]
						- outputTable[i][j].length());
				formatedOutput[i].append(outputTable[i][j]);
				formatedOutput[i].append(TABULATION);
			}
		}
		return formatedOutput;
	}

	private void appendSpacesToStringBuffer(StringBuffer sb, int count) {
		for (int j = 0; j < count; j++) {
			sb.append(' ');
		}
	}
}
