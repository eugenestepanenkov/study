package com.stepsoft.study.configuration;

import com.stepsoft.study.configuration.flow.ExportFlowContext;
import com.stepsoft.study.configuration.flow.ImportFlowContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.config.EnableIntegrationManagement;
import org.springframework.integration.support.management.DefaultMetricsFactory;
import org.springframework.integration.support.management.MetricsFactory;

import static com.stepsoft.study.configuration.utils.ConfigurationConstants.FLOW_METRICS_FACTORY;

/**
 * @author Eugene Stepanenkov
 */
@Configuration
@EnableIntegration
@EnableIntegrationManagement(
        defaultLoggingEnabled = "${flow.monitoring.loggingEnabled}",
        defaultCountsEnabled = "${flow.monitoring.countsEnabled}",
        defaultStatsEnabled = "${flow.monitoring.statsEnabled}",
        countsEnabled = "${flow.monitoring.countsPatterns}",
        statsEnabled = "${flow.monitoring.statsPatterns}",
        metricsFactory = FLOW_METRICS_FACTORY
)
@IntegrationComponentScan(basePackages = {
        "com.stepsoft.study.flow",
        "com.stepsoft.study.configuration.flow"
})
@ComponentScan(basePackages = {
        "com.stepsoft.study.flow",
        "com.stepsoft.study.configuration.flow"
})
@Import({
        ImportFlowContext.class,
        ExportFlowContext.class
})
@PropertySource("classpath:flow.properties")
public class FlowContext {

    @Bean(name = FLOW_METRICS_FACTORY)
    public MetricsFactory flowMetricsFactory() {

        return new DefaultMetricsFactory();
    }
}