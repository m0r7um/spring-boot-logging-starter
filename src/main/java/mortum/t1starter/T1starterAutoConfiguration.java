package mortum.t1starter;

import mortum.t1starter.aspects.HttpLoggingAspect;
import mortum.t1starter.aspects.LoggingAspect;
import mortum.t1starter.properties.LoggingProperties;
import org.slf4j.event.Level;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
@EnableConfigurationProperties(LoggingProperties.class)
@ConditionalOnProperty(name = "logging-starter.enabled", havingValue = "true", matchIfMissing = true)
public class T1starterAutoConfiguration {

    private final LoggingProperties loggingProperties;

    public T1starterAutoConfiguration(LoggingProperties loggingProperties) {
        this.loggingProperties = loggingProperties;
    }


    @Bean
    @ConditionalOnMissingBean(name = "httpLoggingAspect")
    public HttpLoggingAspect httpLoggingAspect() {
        Level loggingLevel = loggingProperties.getLevel();
        return new HttpLoggingAspect(loggingLevel);
    }

    @Bean
    @ConditionalOnMissingBean(name = "loggingAspect")
    public LoggingAspect loggingAspect() {
        return new LoggingAspect();
    }
}
