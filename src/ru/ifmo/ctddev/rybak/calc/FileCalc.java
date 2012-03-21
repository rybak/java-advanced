package ru.ifmo.ctddev.rybak.calc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FileCalc {

	private static final String IO_ERROR_MESSAGE = "FileCalc:\nAn I/O error occurred: ";

	private String inputFileName;
	private String outputFileName;
	private BufferedReader in;
	private FileWriter out;

	public FileCalc(String inputFileName, String outputFileName) {
		this.inputFileName = inputFileName;
		this.outputFileName = outputFileName;
	}

	private void solve() throws IOException {
		List<String> expressions = new ArrayList<String>();

		String line;
		while ((line = in.readLine()) != null) {
			expressions.add(line);
		}

		CalcWriter calcWriter = new CalcWriter(expressions);
		calcWriter.writeToFileWriter(out);
	}

	private void run() {
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(
					inputFileName), "UTF-8"));
			try {
				out = new FileWriter(outputFileName);
				try {
					solve();
				} finally {
					out.close();
				}
			} finally {
				in.close();
			}
		} catch (IOException e) {
			System.err.println(IO_ERROR_MESSAGE + e.getMessage());
		}
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			Locale.setDefault(Locale.US);
			new FileCalc(args[0], args[1]).run();
		} else {
			System.err
					.println("There must be 2 arguments: input filename and outputfilename.");
		}
	}
}
