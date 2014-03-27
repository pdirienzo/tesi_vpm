package org.opendaylight.ovsdb.lib.message;

/**
 * This class has to be extended
 * @author pasquale
 *
 */
public abstract class OvsdbResponse {
	public Error error;
	public String id;
}
