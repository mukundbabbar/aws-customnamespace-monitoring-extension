/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.aws.customnamespace;

import static com.appdynamics.extensions.aws.Constants.METRIC_PATH_SEPARATOR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.appdynamics.extensions.aws.MultipleNamespaceCloudwatchMonitor;
import com.appdynamics.extensions.aws.collectors.NamespaceMetricStatisticsCollector;
import com.appdynamics.extensions.aws.config.ConcurrencyConfig;
import com.appdynamics.extensions.aws.customnamespace.conf.CustomNamespaceAccount;
import com.appdynamics.extensions.aws.customnamespace.conf.CustomNamespaceConfiguration;
import com.appdynamics.extensions.aws.metric.processors.MetricsProcessor;

/**
 * @author Florencio Sarmiento
 *
 */
public class CustomNamespaceMonitor extends MultipleNamespaceCloudwatchMonitor<CustomNamespaceConfiguration> {
	
	private static final Logger LOGGER = Logger.getLogger("com.singularity.extensions.aws.CustomNamespaceMonitor");
	
	private static final String DEFAULT_METRIC_PREFIX = String.format("%s%s%s%s", 
			"Custom Metrics", METRIC_PATH_SEPARATOR, "Amazon Custom Namespace", METRIC_PATH_SEPARATOR);

	public CustomNamespaceMonitor() {
		super(CustomNamespaceConfiguration.class);
		LOGGER.info(String.format("Using AWS Custom Namespace Monitor Version [%s]", 
				this.getClass().getPackage().getImplementationTitle()));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected List<NamespaceMetricStatisticsCollector> getNamespaceMetricStatisticsCollectorList(
			CustomNamespaceConfiguration config) {
		
		List<NamespaceMetricStatisticsCollector> collectors = new ArrayList<NamespaceMetricStatisticsCollector>();
		
		for (CustomNamespaceAccount account : config.getAccounts()) {
			for (String namespace : account.getNamespaces()) {
				MetricsProcessor metricsProcessor = 
						new CustomNamespaceMetricsProcessor(config.getMetricsConfig().getMetricTypes(), 
								config.getMetricsConfig().getExcludeMetrics(), 
								namespace);

				NamespaceMetricStatisticsCollector collector = new NamespaceMetricStatisticsCollector
						.Builder((List) Arrays.asList(account),
								(ConcurrencyConfig) config.getConcurrencyConfig(), 
								config.getMetricsConfig(),
								metricsProcessor)
					.withCredentialsEncryptionConfig(config.getCredentialsDecryptionConfig())
					.withProxyConfig(config.getProxyConfig())
					.build();
				
				collectors.add(collector);
			}
		}
		
		if (collectors.isEmpty()) {
			LOGGER.warn("No namespace is configured for monitoring");
		}
		
		return collectors;
	}

	@Override
	protected int getNoOfNamespaceThreads(CustomNamespaceConfiguration config) {
		return config.getConcurrencyConfig().getNoOfNamespaceThreads();
	}

	@Override
	protected String getMetricPrefix(CustomNamespaceConfiguration config) {
		return DEFAULT_METRIC_PREFIX;
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

}
