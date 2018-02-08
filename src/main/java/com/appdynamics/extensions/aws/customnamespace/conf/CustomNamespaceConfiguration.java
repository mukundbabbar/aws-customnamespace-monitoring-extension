/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.aws.customnamespace.conf;

import java.util.List;

import com.appdynamics.extensions.aws.config.CredentialsDecryptionConfig;
import com.appdynamics.extensions.aws.config.MetricsConfig;
import com.appdynamics.extensions.aws.config.ProxyConfig;

/**
 * @author Florencio Sarmiento
 *
 */
public class CustomNamespaceConfiguration {
	
	private List<CustomNamespaceAccount> accounts;
	
	private CredentialsDecryptionConfig credentialsDecryptionConfig;
	
	private ProxyConfig proxyConfig;
	
	private MetricsConfig metricsConfig;
	
	private CustomNamespaceConcurrencyConfig concurrencyConfig;
	
	private String metricPrefix;

	public List<CustomNamespaceAccount> getAccounts() {
		return accounts;
	}

	public void setAccounts(List<CustomNamespaceAccount> accounts) {
		this.accounts = accounts;
	}

	public CredentialsDecryptionConfig getCredentialsDecryptionConfig() {
		return credentialsDecryptionConfig;
	}

	public void setCredentialsDecryptionConfig(
			CredentialsDecryptionConfig credentialsDecryptionConfig) {
		this.credentialsDecryptionConfig = credentialsDecryptionConfig;
	}

	public ProxyConfig getProxyConfig() {
		return proxyConfig;
	}

	public void setProxyConfig(ProxyConfig proxyConfig) {
		this.proxyConfig = proxyConfig;
	}

	public MetricsConfig getMetricsConfig() {
		return metricsConfig;
	}

	public void setMetricsConfig(MetricsConfig metricsConfig) {
		this.metricsConfig = metricsConfig;
	}

	public CustomNamespaceConcurrencyConfig getConcurrencyConfig() {
		return concurrencyConfig;
	}

	public void setConcurrencyConfig(
			CustomNamespaceConcurrencyConfig concurrencyConfig) {
		this.concurrencyConfig = concurrencyConfig;
	}

	public String getMetricPrefix() {
		return metricPrefix;
	}

	public void setMetricPrefix(String metricPrefix) {
		this.metricPrefix = metricPrefix;
	}

}
