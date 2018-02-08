/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.aws.customnamespace.conf;

import java.util.Set;

import com.appdynamics.extensions.aws.config.Account;

/**
 * @author Florencio Sarmiento
 *
 */
public class CustomNamespaceAccount extends Account {
	
	private Set<String> namespaces;

	public Set<String> getNamespaces() {
		return namespaces;
	}

	public void setNamespaces(Set<String> namespaces) {
		this.namespaces = namespaces;
	}

}
