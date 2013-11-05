package de.zalando.zomcat.cxf.metrics;

import java.io.OutputStream;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.xml.namespace.QName;

import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.FaultMode;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Clock;
import com.codahale.metrics.MetricRegistry;

import com.google.common.base.Preconditions;

import de.zalando.zomcat.cxf.HttpHeaders;
import de.zalando.zomcat.io.StatsCollectorOutputStream;
import de.zalando.zomcat.io.StatsCollectorOutputStreamCallback;

public class MetricsCollector implements MetricsListener {

    /**
     * The logging object for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MetricsCollector.class);

    private final Clock clock = Clock.defaultClock();

    private final MetricRegistry registry;

    public MetricsCollector(final MetricRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void onRequest(final Message message) {
        Preconditions.checkNotNull(message, "message");

        // Gets the HTTP request. It's an error if it's not present in the message.
        final HttpServletRequest request = (HttpServletRequest) message.get(AbstractHTTPDestination.HTTP_REQUEST);
        if (request == null) {
            LOG.error("No HTTP Request found.");
            return;
        }

        // Collect metrics from Message
        long serviceRequestTime = clock.getTick();
        String flowId = HttpHeaders.FLOW_ID.get(request);
        String clientIp = request.getRemoteAddr();
        int requestSize = request.getContentLength();
        String serviceIp = request.getLocalAddr();
        String host = HttpHeaders.HOST.get(request);
        String instance = HttpHeaders.INSTANCE.get(request);
        String serviceName = ((QName) message.get(Message.WSDL_SERVICE)).getLocalPart();
        String operation = ((QName) message.get(Message.WSDL_OPERATION)).getLocalPart();

        String keyPrefix = MetricRegistry.name(serviceName, operation);

        registry.meter(MetricRegistry.name(keyPrefix, MetricsFields.REQUEST_COUNT.toString())).mark();

        if (requestSize != -1) {
            registry.histogram(MetricRegistry.name(keyPrefix, MetricsFields.REQUEST_SIZE.toString())).update(
                requestSize);
        }

        Exchange ex = message.getExchange();

        if (!ex.isOneWay()) {

            // Instantiate metrics and add to Exchange
            WebServiceMetrics metrics = new WebServiceMetrics.Builder().field(MetricsFields.FLOW_ID, flowId)
                                                                       .field(MetricsFields.CLIENT_IP, clientIp)
                                                                       .field(MetricsFields.REQUEST_SIZE, requestSize)
                                                                       .field(MetricsFields.SERVICE_IP, serviceIp)
                                                                       .field(MetricsFields.SERVICE_HOST, host)
                                                                       .field(MetricsFields.SERVICE_INSTANCE, instance)
                                                                       .field(MetricsFields.SERVICE_NAME, serviceName)
                                                                       .field(MetricsFields.SERVICE_OPERATION,
                    operation).field(MetricsFields.REQUEST_TIME, serviceRequestTime).build();
            ex.put(WebServiceMetrics.class, metrics);
        }

        // Log the metrics
        LOG.info("{} {} {} {}:{} {} {} {} null",
            new Object[] {flowId, clientIp, serviceIp, host, instance, serviceName, operation, requestSize});
    }

    @Override
    public void onResponse(final Message cxfMessage) {
        Preconditions.checkNotNull(cxfMessage, "cxfMessage");

        // Get's the HTTP response. It's an error if it's not present in the message.
        final HttpServletResponse response = (HttpServletResponse) cxfMessage.get(
                AbstractHTTPDestination.HTTP_RESPONSE);
        if (response == null) {
            LOG.error("No HTTP Response found.");
            return;
        }

        // Get the response time
        long responseTime = clock.getTick();
        cxfMessage.setContent(OutputStream.class, buildOutputStream(cxfMessage, responseTime));
    }

    @Override
    public void handleFault(final Message message) {
        Preconditions.checkNotNull(message, "message");

        String serviceName = ((QName) message.get(Message.WSDL_SERVICE)).getLocalPart();
        String operation = ((QName) message.get(Message.WSDL_OPERATION)).getLocalPart();

        String keyPrefix = MetricRegistry.name(serviceName, operation);

        FaultMode mode = message.get(FaultMode.class);
        switch (mode) {

            case CHECKED_APPLICATION_FAULT :
                registry.meter(MetricRegistry.name(keyPrefix, MetricsFields.CHECKED_APPLICATION_FAULT.toString()))
                        .mark();
                break;

            case LOGICAL_RUNTIME_FAULT :
                registry.meter(MetricRegistry.name(keyPrefix, MetricsFields.LOGICAL_RUNTIME_FAULT.toString())).mark();
                break;

            case UNCHECKED_APPLICATION_FAULT :
                registry.meter(MetricRegistry.name(keyPrefix, MetricsFields.UNCHECKED_APPLICATION_FAULT.toString()))
                        .mark();
                break;

            case RUNTIME_FAULT :
            default :
                registry.meter(MetricRegistry.name(keyPrefix, MetricsFields.RUNTIME_FAULT.toString())).mark();
        }
    }

    /*
     * Wrap the message's output stream inside a StatsCollector, with the ability to count the response size in
     * bytes.
     */
    protected StatsCollectorOutputStream buildOutputStream(final Message cxfMessage, final long responseTime) {
        StatsCollectorOutputStream statsOs = new StatsCollectorOutputStream(cxfMessage.getContent(OutputStream.class));

        // Register our callback, which will effectively log the metrics when the response size is available.
        statsOs.registerCallback(new MetricsCollectorCallback(cxfMessage, responseTime));

        return statsOs;
    }

    protected class MetricsCollectorCallback implements StatsCollectorOutputStreamCallback {

        private final Message cxfMessage;
        private long responseTime;

        public MetricsCollectorCallback(final Message cxfMessage, final long responseTime) {
            this.cxfMessage = cxfMessage;
            this.responseTime = responseTime;
        }

        @Override
        public void onClose(final StatsCollectorOutputStream os) {
            long responseSize = os.getBytesWritten();

            WebServiceMetrics metrics = cxfMessage.getExchange().get(WebServiceMetrics.class);

            // Update metrics in Exchange
            cxfMessage.getExchange().put(WebServiceMetrics.class, metrics);

            // Calculate execution time
            long executionDelta = responseTime - metrics.get(MetricsFields.REQUEST_TIME);

            String keyPrefix = MetricRegistry.name(metrics.get(MetricsFields.SERVICE_NAME),
                    metrics.get(MetricsFields.SERVICE_OPERATION));

            registry.histogram(MetricRegistry.name(keyPrefix, MetricsFields.RESPONSE_SIZE.toString())).update(
                responseSize);
            registry.timer(MetricRegistry.name(keyPrefix, MetricsFields.DURATION.toString())).update(executionDelta,
                TimeUnit.NANOSECONDS);

            // Output log
            LOG.info("{} {} {} {}:{} {} {} {} {}",
                new Object[] {
                    metrics.get(MetricsFields.FLOW_ID), metrics.get(MetricsFields.CLIENT_IP),
                    metrics.get(MetricsFields.SERVICE_IP), metrics.get(MetricsFields.SERVICE_HOST),
                    metrics.get(MetricsFields.SERVICE_INSTANCE), metrics.get(MetricsFields.SERVICE_NAME),
                    metrics.get(MetricsFields.SERVICE_OPERATION), responseSize, executionDelta
                });
        }
    }
}
