package com.appdynamics.extensions.aws.customnamespace;

import static com.appdynamics.extensions.aws.customnamespace.IntegrationTestUtils.initializeMetricAPIService;
import com.appdynamics.extensions.controller.apiservices.MetricAPIService;
import com.appdynamics.extensions.util.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Prashant Mehta
 */
public class MetricCheckIT {

    private MetricAPIService metricAPIService;

    @Before
    public void setup() {
        metricAPIService = initializeMetricAPIService();
    }

    @Test
    public void testAPICallsMetric() {
        JsonNode jsonNode = null;
        if (metricAPIService != null) {
            jsonNode = metricAPIService.getMetricData("", "Server%20&%20Infrastructure%20Monitoring/metric-data?metric-path=Application%20Infrastructure%20Performance%7CRoot%7CCustom%20Metrics%7CAWS%20Custom%20Namespace%7CAWS%20API%20Calls&time-range-type=BEFORE_NOW&duration-in-mins=15&output=JSON");
        }
        Assert.assertNotNull("Cannot connect to controller API", jsonNode);
        try {
            Assert.assertTrue(jsonNode.toString(), false);
        } catch (AssertionError | Exception e) {
            // Dummy echo output
            System.out.println("Assert error: " + e);
        }
        JsonNode valueNode = JsonUtils.getNestedObject(jsonNode, "*", "metricId");
        Assert.assertTrue("AWS API Calls", valueNode.get(0).asInt() > 0);

    }


}