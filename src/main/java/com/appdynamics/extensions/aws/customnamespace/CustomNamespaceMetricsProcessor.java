package com.appdynamics.extensions.aws.customnamespace;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.Metric;
import com.appdynamics.extensions.aws.config.MetricType;
import com.appdynamics.extensions.aws.metric.NamespaceMetricStatistics;
import com.appdynamics.extensions.aws.metric.StatisticType;
import com.appdynamics.extensions.aws.metric.processors.MetricsProcessor;
import com.appdynamics.extensions.aws.metric.processors.MetricsProcessorHelper;

/**
 * @author Florencio Sarmiento
 *
 */
public class CustomNamespaceMetricsProcessor implements MetricsProcessor {
	
	private String namespace;
	
	private List<MetricType> metricTypes;
	
	private Pattern excludeMetricsPattern;
	
	public CustomNamespaceMetricsProcessor(List<MetricType> metricTypes,
			Set<String> excludeMetrics,
			String namespace) {
		this.metricTypes = metricTypes;
		this.excludeMetricsPattern = MetricsProcessorHelper.createPattern(excludeMetrics);
		this.namespace = namespace;
	}

	public List<Metric> getMetrics(AmazonCloudWatch awsCloudWatch) {
		return MetricsProcessorHelper.getFilteredMetrics(awsCloudWatch, 
				namespace, 
				excludeMetricsPattern);
	}
	
	public StatisticType getStatisticType(Metric metric) {
		return MetricsProcessorHelper.getStatisticType(metric, metricTypes);
	}
	
	public Map<String, Double> createMetricStatsMapForUpload(NamespaceMetricStatistics namespaceMetricStats) {
		return MetricsProcessorHelper.createMetricStatsMapForUpload(namespaceMetricStats, 
				null, true);
	}
	
	public String getNamespace() {
		return namespace;
	}
}
