package controller.lambda;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class Predicates {
	public static <T> Predicate<Function<T, Object>> equalizer(
	        T o1, T o2) {
		if (o1 == null && o2 == null)
		    return (a) -> true;

		if (o1 == null || o2 == null)
		    return (a) -> false;

		return (Function<T, Object> f) -> {
			return (f.apply(o1) == null
			        ? f.apply(o2) == null
			        : f.apply(o1)
			                .equals(f.apply(
			                        o2)));
		};
	}

	public static <T> boolean areEqual(Class<? extends T> c,
	        Object o1, Object o2,
	        List<Function<T, Object>> functions) {

		if (o1 == null && o2 == null) return true;
		if (o1 == null || o2 == null) return false;
		if (!o1.getClass().equals(o2.getClass()))
		    return false;

		Predicate<Function<T, Object>> eq = equalizer(
		        c.cast(o1),
		        c.cast(o2));
		for (Function<T, Object> f : functions) {
			if (!eq.test(f))
			    return false;
		}
		return true;
	}
}
