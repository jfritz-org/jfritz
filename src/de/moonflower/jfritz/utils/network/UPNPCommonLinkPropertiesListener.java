package de.moonflower.jfritz.utils.network;

	/**
	 * This interface is used for setting
	 * the upstream and downstream dsl sync.
	 *
	 *
	 * @author Robert Palmer
	 *
	 */
public interface UPNPCommonLinkPropertiesListener {

	void setUpstreamMaxBitRate(String maxUp);

	void setDownstreamMaxBitRate(String maxDown);

}

