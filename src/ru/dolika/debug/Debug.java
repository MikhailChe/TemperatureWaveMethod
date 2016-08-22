package ru.dolika.debug;

public class Debug {
	public static boolean debug = false;

	public static void print(Object s) {
		if (debug)
			System.out.print(s.toString());
	}

	public static void println(String s) {
		if (debug)
			System.out.println(s);
	}

	public static void println() {
		if (debug)
			System.out.println();
	}
}
