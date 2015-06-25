package com.appdynamics.extensions.aws.customnamespace.conf;

import com.appdynamics.extensions.aws.config.ConcurrencyConfig;

/**
 * @author Florencio Sarmiento
 *
 */
public class CustomNamespaceConcurrencyConfig extends ConcurrencyConfig {
	
	private int noOfNamespaceThreads;

	public int getNoOfNamespaceThreads() {
		return noOfNamespaceThreads;
	}

	public void setNoOfNamespaceThreads(int noOfNamespaceThreads) {
		this.noOfNamespaceThreads = noOfNamespaceThreads;
	}
	
}
