package ru.ifmo.ctddev.rybak.calc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class Function {
	private BufferedReader in;
	private FileWriter out;
	private String expression;

	private static final String IO_ERROR_MESSAGE = "Function:\nAn I/O error occurred: ";

	public Function(File file) {
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(
					file), "UTF-8"));
			try {
				expression = in.readLine();
			} finally {
				in.close();
			}
		} catch (IOException e) {
			System.err.println(IO_ERROR_MESSAGE + e.getMessage());
		}
	}

	public void write(File file) {
		try {
			out = new FileWriter(file);
			try {
				CalcWriter calcWriter = new CalcWriter(expression);
				calcWriter.writeToFileWriter(out);
			} finally {
				out.close();
			}
		} catch (IOException e) {
			System.err.println(IO_ERROR_MESSAGE + e.getMessage());
		}
	}
}
