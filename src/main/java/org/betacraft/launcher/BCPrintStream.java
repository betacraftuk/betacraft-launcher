package org.betacraft.launcher;

import java.io.OutputStream;
import java.io.PrintStream;

public class BCPrintStream extends PrintStream {
	private String type;
	private PrintStream original;
	private boolean printOriginalFormatted;

	public BCPrintStream(PrintStream original, OutputStream out, String type, boolean printOriginalFormatted) {
		super(out, true);

		// out / err
		this.type = type;
		// original console stream
		this.original = original;

		this.printOriginalFormatted = printOriginalFormatted;
	}

	@Override
	public void println() {
		String msg = "[" + Logger.format.format(Long.valueOf(System.currentTimeMillis())) + "] " + this.type;
		super.println(msg);
		if (printOriginalFormatted)
			this.original.println(msg);
		else
			this.original.println();
	}

	@Override
	public void println(String s) {
		String msg = "[" + Logger.format.format(Long.valueOf(System.currentTimeMillis())) + "] " + this.type + " " + s;
		super.println(msg);
		if (printOriginalFormatted)
			this.original.println(msg);
		else
			this.original.println(s);
	}

	@Override
	public void println(Object o) {
		super.println(o);
		this.original.println(o);
	}
}
