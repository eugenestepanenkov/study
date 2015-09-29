package com.stepsoft.study.configuration.flow;

import com.stepsoft.study.configuration.annotation.JpaGateway;
import com.stepsoft.study.data.entity.Sinner;
import com.stepsoft.study.flow.ProcessingService;
import com.stepsoft.study.flow.messaging.ImportAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.BridgeFrom;
import org.springframework.integration.annotation.BridgeTo;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Router;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.jpa.core.JpaExecutor;
import org.springframework.integration.jpa.outbound.JpaOutboundGateway;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.Header;

import static com.stepsoft.study.configuration.utils.ConfigurationConstants.IMPORT_ACTION;
import static com.stepsoft.study.configuration.utils.ConfigurationConstants.IN_IMPORT_ADD_OR_UPDATE_DB_CHANNEL;
import static com.stepsoft.study.configuration.utils.ConfigurationConstants.IN_IMPORT_CHANNEL;
import static com.stepsoft.study.configuration.utils.ConfigurationConstants.IN_IMPORT_DB_CHANNEL;
import static com.stepsoft.study.configuration.utils.ConfigurationConstants.IN_IMPORT_DELETE_DB_CHANNEL;
import static com.stepsoft.study.configuration.utils.ConfigurationConstants.IN_IMPORT_FETCH_DB_CHANNEL;
import static com.stepsoft.study.configuration.utils.ConfigurationConstants.IN_IMPORT_PROCESSING_CHANNEL;
import static com.stepsoft.study.configuration.utils.ConfigurationConstants.IN_IMPORT_TRANSFORMER_CHANNEL;
import static com.stepsoft.study.configuration.utils.ConfigurationConstants.OUT_IMPORT_DB_CHANNEL;
import static com.stepsoft.study.configuration.utils.ConfigurationConstants.OUT_IMPORT_PROCESSING_CHANNEL;
import static org.springframework.integration.jpa.support.OutboundGatewayType.RETRIEVING;
import static org.springframework.integration.jpa.support.OutboundGatewayType.UPDATING;

/**
 * @author Eugene Stepanenkov
 */
@Configuration
@Import(ImportChannelContext.class)
public class ImportFlowContext {

    @MessageEndpoint
    public static class InImportRouterEndpoint {

        @Router(inputChannel = IN_IMPORT_CHANNEL)
        public String route(@Header(name = IMPORT_ACTION) ImportAction action) {

            switch (action) {

                case DELETE:
                case FETCH:
                    return IN_IMPORT_DB_CHANNEL;

                case ADD_BULK:
                case ADD:
                case UPDATE:
                    return IN_IMPORT_TRANSFORMER_CHANNEL;

                default:
                    throw new IllegalStateException();
            }
        }
    }

    @MessageEndpoint
    public static class InImportTransformerEndpoint {

        @Transformer(inputChannel = IN_IMPORT_TRANSFORMER_CHANNEL, outputChannel = IN_IMPORT_DB_CHANNEL)
        public Sinner transform(Sinner sinner) {

            return sinner;
        }
    }

    @MessageEndpoint
    public static class InImportDbRouterEndpoint {

        @Router(inputChannel = IN_IMPORT_DB_CHANNEL)
        public String route(@Header(name = IMPORT_ACTION) ImportAction action) {

            switch (action) {

                case DELETE:
                    return IN_IMPORT_DELETE_DB_CHANNEL;

                case FETCH:
                    return IN_IMPORT_FETCH_DB_CHANNEL;

                case ADD:
                case UPDATE:
                    return IN_IMPORT_ADD_OR_UPDATE_DB_CHANNEL;

                case ADD_BULK:
                    return IN_IMPORT_PROCESSING_CHANNEL;

                default:
                    throw new IllegalStateException();
            }
        }
    }

    @MessageEndpoint
    public static class ProcessingServiceEndpoint {

        @Autowired
        private ProcessingService serviceActivator;

        @ServiceActivator(inputChannel = IN_IMPORT_PROCESSING_CHANNEL, outputChannel = OUT_IMPORT_PROCESSING_CHANNEL)
        public Message<Sinner> serviceMethod(Message<Sinner> message) {

            return serviceActivator.service(message);
        }
    }

    @Bean
    @BridgeFrom(OUT_IMPORT_PROCESSING_CHANNEL)
    public MessageChannel afterProcessingBridgeFrom() {

        return new DirectChannel();
    }

    @Bean
    @BridgeTo(IN_IMPORT_ADD_OR_UPDATE_DB_CHANNEL)
    public MessageChannel afterProcessingBridgeTo() {

        return new DirectChannel();
    }

    @Bean
    @Autowired
    @JpaGateway(inputChannel = IN_IMPORT_ADD_OR_UPDATE_DB_CHANNEL)
    public JpaOutboundGateway inImportAddOrUpdateDbGateway(JpaExecutor importAddOrUpdateJpaExecutor) {

        JpaOutboundGateway gateway = new JpaOutboundGateway(importAddOrUpdateJpaExecutor);
        gateway.setOutputChannelName(OUT_IMPORT_DB_CHANNEL);
        gateway.setGatewayType(UPDATING);

        return gateway;
    }

    @Bean
    @Autowired
    @JpaGateway(inputChannel = IN_IMPORT_FETCH_DB_CHANNEL)
    public JpaOutboundGateway inImportFetchDbGateway(JpaExecutor importFetchJpaExecutor) {

        JpaOutboundGateway gateway = new JpaOutboundGateway(importFetchJpaExecutor);
        gateway.setOutputChannelName(OUT_IMPORT_DB_CHANNEL);
        gateway.setGatewayType(RETRIEVING);
        gateway.setRequiresReply(true);

        return gateway;
    }

    @Bean
    @Autowired
    @JpaGateway(inputChannel = IN_IMPORT_DELETE_DB_CHANNEL)
    public JpaOutboundGateway inImportDeleteDbGateway(JpaExecutor importDeleteJpaExecutor) {

        JpaOutboundGateway gateway = new JpaOutboundGateway(importDeleteJpaExecutor);
        gateway.setOutputChannelName(OUT_IMPORT_DB_CHANNEL);
        gateway.setGatewayType(UPDATING);

        return gateway;
    }

    @MessageEndpoint
    public static class OutImportDbRouterEndpoint {

        @Router(inputChannel = OUT_IMPORT_DB_CHANNEL)
        public String route(@Header(name = IMPORT_ACTION) ImportAction action) {

            throw new IllegalArgumentException();
        }
    }
/*
    @MessageEndpoint
    public static class OutImportTransformerEndpoint {

        @Transformer(inputChannel = OUT_IMPORT_TRANSFORMER_CHANNEL, outputChannel = OUT_IMPORT_DB_CHANNEL)
        public Message<SinnerModel> transform(Message<SinnerModel> message) {

            message.getHeaders().put(IS_TRANSFORMED_TO_DB_DTO, false);
            return message;
        }
    }

    @MessageEndpoint
    public static class OutImportAggregatorEndpoint {

        @Aggregator(inputChannel = OUT_IMPORT_AGGREGATOR_CHANNEL, outputChannel = OUT_IMPORT_CHANNEL)
        public Set<Long> aggregate(Set<Long> ids) {

            return ids;
        }

        @ReleaseStrategy
        public boolean releaseStrategy(List<Message<Long>> messages) {

            int bulkSize = parseInt((String) messages.get(0).getHeaders().get(BULK_SIZE));
            return bulkSize == messages.size();
        }

        @CorrelationStrategy
        public String correlationStrategy(@Header(name = BULK_SIZE) String bulkSize) {

            return bulkSize;
        }
    }*/
}
