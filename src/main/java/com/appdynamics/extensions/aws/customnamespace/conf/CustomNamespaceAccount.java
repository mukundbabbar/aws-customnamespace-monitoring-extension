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
