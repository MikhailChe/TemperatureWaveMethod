package debug;

public class Debug {

	private static boolean debug = true;

	public static boolean isDebug() {
		return debug;
	}

	public static void print(Object s) {
		if (s != null)
			if (debug)
				System.out.print(s.toString());
	}

	public static void println(Object o) {
		if (o != null)
			println(o.toString());
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
