/*
 * Copyright 2020. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.aws.customnamespace;

import static com.appdynamics.extensions.aws.Constants.METRIC_PATH_SEPARATOR;
import com.appdynamics.extensions.aws.MultipleNamespaceCloudwatchMonitor;
import com.appdynamics.extensions.aws.collectors.NamespaceMetricStatisticsCollector;
import com.appdynamics.extensions.aws.customnamespace.conf.AWSAccount;
import com.appdynamics.extensions.aws.customnamespace.conf.CustomNamespaceConfiguration;
import com.appdynamics.extensions.aws.metric.processors.MetricsProcessor;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.collect.Lists;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Florencio Sarmiento
 */
public class CustomNamespaceMonitor extends MultipleNamespaceCloudwatchMonitor<CustomNamespaceConfiguration> {

	private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger("CustomNamespaceMonitor");

	private static final String DEFAULT_METRIC_PREFIX = String.format("%s%s%s%s",
			"Custom Metrics", METRIC_PATH_SEPARATOR, "AWS Custom Namespace", METRIC_PATH_SEPARATOR);

	public CustomNamespaceMonitor() {
		super(CustomNamespaceConfiguration.class);
		LOGGER.info(String.format("Using AWS Custom Namespace Monitor Version [%s]",
				this.getClass().getPackage().getImplementationTitle()));
	}

	@Override
	protected String getDefaultMetricPrefix() {
		return DEFAULT_METRIC_PREFIX;
	}

	@Override
	public String getMonitorName() {
		return "AWSCustomNamespaceMonitor";
	}

	@Override
	protected List<Map<String, ?>> getServers() {
		return Lists.newArrayList();
	}

	@Override
	protected List<NamespaceMetricStatisticsCollector> getNamespaceMetricStatisticsCollectorList(CustomNamespaceConfiguration config) {

		List<NamespaceMetricStatisticsCollector> collectors = Lists.newArrayList();

		for (AWSAccount account : config.getAwsAccounts()) {
				MetricsProcessor metricsProcessor = new CustomNamespaceMetricsProcessor(config, account);

				NamespaceMetricStatisticsCollector collector = new NamespaceMetricStatisticsCollector
						.Builder(Arrays.asList(account),
                                config.getConcurrencyConfig(),
								config.getMetricsConfig(),
								metricsProcessor, config.getMetricPrefix())
					.withCredentialsDecryptionConfig(config.getCredentialsDecryptionConfig())
					.withProxyConfig(config.getProxyConfig())
					.build();

				collectors.add(collector);
			}

		if (collectors.isEmpty()) {
			LOGGER.warn("No namespace is configured for monitoring");
		}

		return collectors;
	}

	@Override
	protected int getNoOfNamespaceThreads(CustomNamespaceConfiguration config) {
		return config.getConcurrencyConfig().getNoOfAccountThreads();
	}

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

//    public static void main(String[] args) throws TaskExecutionException {
//
//        CustomNamespaceMonitor monitor = new CustomNamespaceMonitor();
//
//        final Map<String, String> taskArgs = new HashMap<String, String>();
//
//        taskArgs.put("config-file", "src/main/resources/conf/config.yml");
//        taskArgs.put("metric-file", "src/main/resources/conf/metrics.xml");
//		monitor.execute(taskArgs, null);
//    }

}
