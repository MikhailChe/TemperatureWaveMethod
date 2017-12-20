package debug;

import static java.lang.System.out;

public class Debug {

	private static boolean debug = true;

	public static boolean isDebug() {
		return debug;
	}

	public static <T> void print(T o) {
		if (debug)
			if (o != null)
				out.print(o.toString());
			else
				out.print("null");
	}

	public static <T> void println(T o) {
		if (debug) {
			if (o != null)
				out.println(o.toString());
			else
				out.println("null");
		}
	}

	public static void println() {
		if (debug)
			out.println();
	}
}
