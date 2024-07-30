package org.programmingtechie.config;
import io.jaegertracing.Configuration;
import io.opentracing.Tracer;
import org.springframework.context.annotation.Bean;


@org.springframework.context.annotation.Configuration
public class TracingConfig
{
    @Bean
    public Tracer jaegerTracer() {
        return new Configuration("product-service")
                .withSampler(Configuration.SamplerConfiguration.fromEnv().withType("const").withParam(1))
                .withReporter(Configuration.ReporterConfiguration.fromEnv().withLogSpans(true))
                .getTracer();
    }
}
