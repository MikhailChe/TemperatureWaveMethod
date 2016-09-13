package controller.lambda;

public class HashCoder {
	public static int hashCode(Object... o) {
		int code = 0;
		for (Object a : o) {
			if (a != null)
			    code += a.hashCode();
		}
		return code;
	}
}
