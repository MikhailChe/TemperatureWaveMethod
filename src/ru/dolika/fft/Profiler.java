package ru.dolika.fft;

import java.util.HashMap;

public class Profiler {

	private HashMap<String, Long> execTime;
	private HashMap<String, Long> startTime;

	private Profiler() {
		execTime = new HashMap<String, Long>();
		startTime = new HashMap<String, Long>();
	}

	private static Profiler me = null;

	public synchronized static Profiler getInstance() {
		if (me == null) {
			me = new Profiler();
		}
		return me;
	}

	public void startProfiler() {
		StackTraceElement el = Thread.currentThread().getStackTrace()[2];
		startTime.put((el.getClassName() + "." + el.getMethodName()),
				System.currentTimeMillis());
	}

	public long stopProfiler() {
		long curTime = System.currentTimeMillis();
		StackTraceElement el = Thread.currentThread().getStackTrace()[2];
		String function = el.getClassName() + "." + el.getMethodName();
		Long stt = startTime.get(function);
		if (stt != null) {
			long simpstt = stt;
			if (execTime.containsKey(function)) {
				long oldExecTime = execTime.get(function);
				long newExecTime = (oldExecTime + (curTime - simpstt)) / 2;
				execTime.put(function, newExecTime);
				return newExecTime;
			} else {
				long newExecTime = curTime - simpstt;

				execTime.put(function, newExecTime);
				return newExecTime;
			}
		}
		return 0;
	}

	public String toString() {
		return execTime.toString();
	}
}
