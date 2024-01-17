package it.magiavventure.common.configuration;

import it.magiavventure.common.filter.RequestResponseLoggingFilter;
import it.magiavventure.common.filter.TransactionIdFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan("it.magiavventure")
@PropertySource({"classpath:default-application.properties"})
public class CommonConfig {

    @Bean
    public RequestResponseLoggingFilter requestResponseLoggingFilter() {
        return new RequestResponseLoggingFilter();
    }

    @Bean
    public TransactionIdFilter transactionIdFilter() { return new TransactionIdFilter(); }
}
