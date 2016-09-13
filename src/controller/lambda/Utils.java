package controller.lambda;

public class Utils {
	public static String stringOfObject(Object... objects) {
		StringBuilder sb = new StringBuilder();
		for (Object o : objects) {
			if (o != null)
			    sb.append(o.toString() + " ");
		}
		return sb.toString();
	}
}
