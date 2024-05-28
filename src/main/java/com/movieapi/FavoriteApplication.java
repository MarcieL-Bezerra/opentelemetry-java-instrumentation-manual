package com.movieapi;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.GlobalLoggerProvider;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class FavoriteApplication {
	private static final Logger logger = LoggerFactory.getLogger(FavoriteApplication.class);

	public static void main(String[] args) {

		String SERVICE_NAME = "favorite";

		// set service name on all OTel signals
		Resource resource = Resource.getDefault().merge(Resource.create(
				Attributes.of(ResourceAttributes.SERVICE_NAME, SERVICE_NAME,ResourceAttributes.SERVICE_VERSION,"1.0",ResourceAttributes.DEPLOYMENT_ENVIRONMENT,"production")));

		// init OTel logger provider with export to OTLP
		SdkLoggerProvider sdkLoggerProvider = SdkLoggerProvider.builder()
				.setResource(resource)
				.addLogRecordProcessor(BatchLogRecordProcessor.builder(
								OtlpGrpcLogRecordExporter.builder().setEndpoint("http://localhost:4317")
										.build()).build())
				.build();

		// init OTel trace provider with export to OTLP
		SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
				.setResource(resource).setSampler(Sampler.alwaysOn())
				// add span processor to add baggage as span attributes
				.addSpanProcessor(BatchSpanProcessor.builder(OtlpGrpcSpanExporter
						.builder()
						.setEndpoint("http://localhost:4317")
						.build()).build())
				.build();

		// init OTel meter provider with export to OTLP
		SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
				.setResource(resource)
				.registerMetricReader(PeriodicMetricReader.builder(
								OtlpGrpcMetricExporter.builder().setEndpoint("http://localhost:4317")
						.build()).build())
				.build();

		// create sdk object and set it as global
		OpenTelemetrySdk sdk = OpenTelemetrySdk.builder()
				.setTracerProvider(sdkTracerProvider)
				.setLoggerProvider(sdkLoggerProvider)
				.setMeterProvider(sdkMeterProvider)
				.setPropagators(ContextPropagators
						.create(W3CTraceContextPropagator.getInstance()))
				.build();

		GlobalOpenTelemetry.set(sdk);
		// connect logger
		GlobalLoggerProvider.set(sdk.getSdkLoggerProvider());
		// Add hook to close SDK, which flushes logs
		Runtime.getRuntime().addShutdownHook(new Thread(sdk::close));

		SpringApplication.run(FavoriteApplication.class, args);
	}
}
