package it.unina.cini.platino.monitoring;

import java.io.IOException;

public interface NodeStatus {
	/**
	 * 
	 * @param measurementInterval the interval between two measurements
	 * 
	 * @return a float indicating the overall CPU % usage, that is a combination
	 * of single CPU usages for a multiprocessor architecture
	 */
	public float getOverallCPUUsage(int measurementInterval) throws IOException;
}
