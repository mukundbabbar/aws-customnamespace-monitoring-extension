/*
 * Copyright 2020. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.aws.customnamespace;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.DimensionFilter;
import com.appdynamics.extensions.aws.config.Dimension;
import com.appdynamics.extensions.aws.config.IncludeMetric;
import com.appdynamics.extensions.aws.config.MetricsTimeRange;
import com.appdynamics.extensions.aws.customnamespace.conf.AWSAccount;
import com.appdynamics.extensions.aws.customnamespace.conf.CustomNamespaceConfiguration;
import com.appdynamics.extensions.aws.dto.AWSMetric;
import com.appdynamics.extensions.aws.metric.NamespaceMetricStatistics;
import com.appdynamics.extensions.aws.metric.StatisticType;
import com.appdynamics.extensions.aws.metric.processors.MetricsProcessor;
import com.appdynamics.extensions.aws.metric.processors.MetricsProcessorHelper;
import com.appdynamics.extensions.aws.predicate.MultiDimensionPredicate;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.util.StringUtils;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Prashant Mehta
 */
public class CustomNamespaceMetricsProcessor implements MetricsProcessor {

    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger("CustomNamespaceMetricsProcessor");
    private List<IncludeMetric> includeMetrics;
    private List<Dimension> dimensions;
    private String namespace;
    private MetricsTimeRange timeRange;
    private CustomNamespaceConfiguration config;

    private List<IncludeMetric> accountIncludeMetrics = Lists.newArrayList();

    public CustomNamespaceMetricsProcessor(CustomNamespaceConfiguration config, String namespace) {
        this.config = config;
        this.includeMetrics = config.getMetricsConfig().getIncludeMetrics();
        this.dimensions = config.getDimensions();
        this.namespace = namespace;
        this.timeRange = config.getMetricsConfig().getMetricsTimeRange();
    }

    @Override
    public List<AWSMetric> getMetrics(AmazonCloudWatch awsCloudWatch, String accountName, LongAdder awsRequestsCounter) {
        List<DimensionFilter> dimensionFilters = getDimensionFilters();
        MultiDimensionPredicate predicate = new MultiDimensionPredicate(dimensions);
        List<com.amazonaws.services.cloudwatch.model.Metric> listMetrics = MetricsProcessorHelper.getMetrics(awsCloudWatch, awsRequestsCounter, namespace, dimensionFilters);
        listMetrics = Lists.newArrayList(Collections2.filter(listMetrics, predicate));

//      Building account AWSMetric. Give priority of the global metricConfig includeMetrics, then account IncludeMetrics
        if (includeMetrics != null) {
            LOGGER.debug("processing the includeMetrics from metricConfigs");
            accountIncludeMetrics.addAll(includeMetrics);
        } else {
            Set<String> queriedAWSListMetricsSet = getMetricsSet(listMetrics);
            Set<String> accountMetrics = getConfiguredAccountMetrics(config, accountName);
            for (String accountMetric : accountMetrics)
                if (StringUtils.hasText(accountMetric))
                    buildAccountIncludeMetrics(queriedAWSListMetricsSet, accountMetric);
            LOGGER.debug("built the configured AWSMetrics from AccountMetrics");
        }
        LOGGER.debug("Total AWSMetrics for processing :", accountIncludeMetrics.size());
        return MetricsProcessorHelper.filterMetrics(listMetrics, accountIncludeMetrics);
    }

    private void buildAccountIncludeMetrics(Set<String> queriedAWSListMetricsSet, String accountMetric) {
        for (String metricName : queriedAWSListMetricsSet) {
            if (matchNameOrPattern(accountMetric, metricName)) {
//              match and build includeMetric with default props
                IncludeMetric incAccountMetric = new IncludeMetric();
                incAccountMetric.setName(metricName);
                incAccountMetric.setAlias(metricName);
                incAccountMetric.setMetricsTimeRange(timeRange);
                incAccountMetric.setStatType("ave");
                accountIncludeMetrics.add(incAccountMetric);
            }
        }
    }

    @Override
    public StatisticType getStatisticType(AWSMetric awsMetric) {
        return MetricsProcessorHelper.getStatisticType(awsMetric.getIncludeMetric(), accountIncludeMetrics);
    }

    private List<DimensionFilter> getDimensionFilters() {
        List<DimensionFilter> dimensionFilters = Lists.newArrayList();
        for (Dimension dimension : dimensions) {
            DimensionFilter dimensionFilter = new DimensionFilter();
            dimensionFilter.withName(dimension.getName());
            dimensionFilters.add(dimensionFilter);
        }
        return dimensionFilters;
    }

    private Set<String> getConfiguredAccountMetrics(CustomNamespaceConfiguration config, String accountName) {
        Set<String> accountMetrics = Sets.newHashSet();
        for (AWSAccount account : config.getAwsAccounts()) {
            if (account.getDisplayAccountName().equals(accountName))
                return account.getAccountMetrics();
        }
        return accountMetrics;
    }

    private boolean matchNameOrPattern(String pattern, String metricName) {
        Pattern regexPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher regexMatcher = regexPattern.matcher(metricName);
        return regexMatcher.matches();
    }

    private Set<String> getMetricsSet(List<com.amazonaws.services.cloudwatch.model.Metric> metrics) {
        Set<String> metricsSet = Sets.newHashSet();
        for (com.amazonaws.services.cloudwatch.model.Metric metric : metrics)
            metricsSet.add(metric.getMetricName());
        return metricsSet;
    }

    public List<Metric> createMetricStatsMapForUpload(NamespaceMetricStatistics namespaceMetricStats) {
        Map<String, String> dimensionToMetricPathNameDictionary = new HashMap<String, String>();
        for (Dimension dimension : dimensions) {
            dimensionToMetricPathNameDictionary.put(dimension.getName(), dimension.getDisplayName());
        }

        return MetricsProcessorHelper.createMetricStatsMapForUpload(namespaceMetricStats,
                dimensionToMetricPathNameDictionary, true);
    }

    public String getNamespace() {
        return namespace;
    }
}