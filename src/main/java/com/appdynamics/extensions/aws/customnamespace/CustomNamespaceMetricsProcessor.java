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

import java.util.Arrays;
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
    private AWSAccount account;
    private MetricsTimeRange timeRange;
    private CustomNamespaceConfiguration config;

    private List<IncludeMetric> accountIncludeMetrics = Lists.newArrayList();

    public CustomNamespaceMetricsProcessor(CustomNamespaceConfiguration config, AWSAccount account) {
        this.config = config;
        this.account = account;
        this.includeMetrics = config.getMetricsConfig().getIncludeMetrics();
        this.timeRange = config.getMetricsConfig().getMetricsTimeRange();
    }

    @Override
    public synchronized List<AWSMetric> getMetrics(AmazonCloudWatch awsCloudWatch, String accountName, LongAdder awsRequestsCounter) {
        handleDimensions();
        List<DimensionFilter> dimensionFilters = getDimensionFilters();
        MultiDimensionPredicate predicate = new MultiDimensionPredicate(dimensions);
        List<com.amazonaws.services.cloudwatch.model.Metric> listMetrics = MetricsProcessorHelper.getMetrics(awsCloudWatch, awsRequestsCounter, account.getNamespace(), dimensionFilters);
        if (dimensions.size() > 0)
            listMetrics = Lists.newArrayList(Collections2.filter(listMetrics, predicate));

//      Building account AWSMetric. Give priority of the global metricConfig includeMetrics, then account IncludeMetrics
        if (includeMetrics != null) {
            LOGGER.debug("processing the includeMetrics from metricConfigs");
            accountIncludeMetrics.addAll(includeMetrics);
        } else {
            Set<String> queriedAWSListMetricsSet = getMetricsSet(listMetrics);
            Set<String> accountMetrics = account.getAccountMetrics();
            for (String accountMetric : accountMetrics)
                if (StringUtils.hasText(accountMetric))
                    buildAccountIncludeMetrics(queriedAWSListMetricsSet, accountMetric);
            LOGGER.debug("built the configured AWSMetrics from AccountMetrics");
        }
        LOGGER.debug("Total AWSMetrics for processing :", accountIncludeMetrics.size());
        return MetricsProcessorHelper.filterMetrics(listMetrics, accountIncludeMetrics);
    }

    @Override
    public synchronized StatisticType getStatisticType(AWSMetric awsMetric) {
        return MetricsProcessorHelper.getStatisticType(awsMetric.getIncludeMetric(), accountIncludeMetrics);
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

    private List<DimensionFilter> getDimensionFilters() {
        List<DimensionFilter> dimensionFilters = Lists.newArrayList();
        for (Dimension dimension : dimensions) {
            DimensionFilter dimensionFilter = new DimensionFilter();
            dimensionFilter.withName(dimension.getName());
            dimensionFilters.add(dimensionFilter);
        }
        return dimensionFilters;
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

    /*
     * 1. account ==> namespaceDimensions [no regex support]
     * 2. dimensions
     * if namespaceDimensions == null/empty/.* ==> listMetrics w/o dimension
     * else
     *       if dimension is null or empty or namespaceDimension doesn't match fetch all as per the namespaceDimensions with value .*.
     *       if the namespaceDimensions exist in dimensions
     *                       ==> match and fetch as per the value
     * */
    private void handleDimensions() {
        List<Dimension> configDimensions = config.getDimensions();
        Set<String> namespaceDimensions = account.getNamespaceDimensions();
        dimensions = Lists.newArrayList();
        if (freeDimension(namespaceDimensions)) {
            return;
        } else {
            if (configDimensions == null || configDimensions.size() == 0) {
                buildNamespaceDimensionsWithDotStar(namespaceDimensions);
                return;
            } else {
                for (String namespaceDimensionStr : namespaceDimensions) {
                    boolean isAdded = false;
                    for (Dimension configDimension : configDimensions) {
                        if (configDimension.getName().equals(namespaceDimensionStr)) {
                            dimensions.add(configDimension);
                            isAdded = true;
                            break;
                        }
                    }
                    if (!isAdded)
                        dimensions.add(getDimensionWithDotStar(namespaceDimensionStr));
                }
            }
        }
    }

    private boolean freeDimension(Set<String> namespaceDimensions) {
        if (namespaceDimensions == null || namespaceDimensions.size() == 0)
            return true;
        if (namespaceDimensions.size() == 1) {
            String dimension = namespaceDimensions.iterator().next().trim();
            if (dimension.equals(".*") || !StringUtils.hasText(dimension))
                return true;

        }
        return false;
    }

    private void buildNamespaceDimensionsWithDotStar(Set<String> namespaceDimensions) {
        for (String dimensionStr : namespaceDimensions)
            dimensions.add(getDimensionWithDotStar(dimensionStr));
    }

    private Dimension getDimensionWithDotStar(String name) {
        Dimension dimension = new Dimension();
        dimension.setName(name);
        dimension.setDisplayName(name);
        dimension.setValues(Sets.newHashSet(Arrays.asList(".*")));
        return dimension;
    }

    public List<Metric> createMetricStatsMapForUpload(NamespaceMetricStatistics namespaceMetricStats) {
        Map<String, String> dimensionToMetricPathNameDictionary = new HashMap<String, String>();
        for (Dimension dimension : dimensions) {
            dimensionToMetricPathNameDictionary.put(dimension.getName(), dimension.getDisplayName());
        }

        return MetricsProcessorHelper.createMetricStatsMapForUpload(namespaceMetricStats,
                dimensionToMetricPathNameDictionary, true);
    }

    @Override
    public String getNamespace() {
        return account.getNamespace();
    }
}