/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

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
