package ru.ifmo.ctddev.rybak.calc;

import java.io.File;

public class Calc {

	private String input;
	private String output;

	public Calc(String inputFileName, String outputFileName) {
		this.input = inputFileName;
		this.output = outputFileName;
	}

	private void solve() {
		Function f = new Function(new File(input));
		f.write(new File(output));
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new Calc(args[0], args[1]).solve();
		} else {
			System.err
					.print("There must be 2 arguments: input filename and outputfilename.");
		}
	}
}
