package it.unina.cini.platino.monitoring;

import java.io.IOException;

/**
 * Interface to be implemented by classes wanting to communicate a Node's status
 * 
 * 
 * <p> 
 * Copyright (C) 2014 University of Naples. All Rights Reserved.
 * <p>
 * This program is distributed under GPL Version 2.0, WITHOUT ANY WARRANTY
 * 
 * @author <a href="mailto:p.dirienzo@studenti.unina.it">p.dirienzo@studenti.unina.it</a>, 
 * <a href="mailto:enr.demaio@studenti.unina.it">enr.demaio@studenti.unina.it</a>
 * @version 1.0
 */
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
