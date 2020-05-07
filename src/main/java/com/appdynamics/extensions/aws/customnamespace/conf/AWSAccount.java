/*
 * Copyright 2020. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.aws.customnamespace.conf;

import com.appdynamics.extensions.aws.config.Account;

import java.util.Set;

/**
 * @author Prashant Mehta
 *
 */
public class AWSAccount extends Account {
	private String namespace;

    private Set<String> accountMetrics;

    private Set<String> namespaceDimensions;

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespaces) {
		this.namespace = namespaces;
	}

	public Set<String> getAccountMetrics() {
		return accountMetrics;
	}

	public void setAccountMetrics(Set<String> accountMetrics) {
		this.accountMetrics = accountMetrics;
	}

	public Set<String> getNamespaceDimensions() {
		return namespaceDimensions;
	}

	public void setNamespaceDimensions(Set<String> namespaceDimensions) {
		this.namespaceDimensions = namespaceDimensions;
	}
}
