package de.moonflower.jfritz.utils;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

public class ConsoleAndFilePrintStream extends PrintStream {
    PrintStream console;

    /** Creates a new instance of SystemOutTest */
    public ConsoleAndFilePrintStream(OutputStream fileOutputStream)
    	throws FileNotFoundException,SecurityException
    {
        super(fileOutputStream);
        // save the handle to the old System.out.
        console = System.err;
    }

    public void flush() {
    	super.flush();
    	console.flush();
    }

    public void close() {
    	super.close();
    	console.close();
    }

    public boolean checkError() {
    	boolean err1 = super.checkError();
    	boolean err2 = console.checkError();
    	return err1 || err2;
    }

    public void println() {
    	super.println();
    	console.println();
    }

    public void println(boolean b) {
    	super.println(b);
    	console.println(b);
    }

    public void println(char c) {
    	super.println(c);
    	console.println(c);
    }

	public void println(int i) {
		super.println(i);
		console.println(i);
	}

	public void println(long l) {
		super.println(l);
		console.println(l);
	}

	public void println(float f) {
		super.println(f);
		console.println(f);
	}

    public void println(double d) {
    	super.println(d);
    	console.println(d);
    }

    public void println(char s[]) {
    	super.println(s);
    	console.println(s);
    }

    public void println(String s) {
    	console.println(s);
    	super.println(s);
    }

    public void println(Object o) {
    	super.println(o);
    	console.println(o);
    }

    public PrintStream printf(String format, Object ... args) {
    	super.printf(format, args);
    	return console.printf(format, args);
    }

    public PrintStream printf(Locale l, String format, Object ... args) {
    	super.printf(l, format, args);
    	return console.printf(l, format, args);
    }

    public PrintStream format(String format, Object ... args) {
    	super.format(format, args);
    	return console.format(format, args);
    }

    public PrintStream format(Locale l, String format, Object ... args) {
    	super.format(l, format, args);
    	return console.format(l, format, args);
    }

    public PrintStream append(CharSequence csq) {
    	super.append(csq);
    	return console.append(csq);
    }

    public PrintStream append(char c) {
    	super.append(c);
    	return console.append(c);
    }

    public PrintStream append(CharSequence csq, int start, int end) {
    	super.append(csq, start, end);
    	return console.append(csq, start, end);
    }
}
