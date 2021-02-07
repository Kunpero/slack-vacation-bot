package rs.kunpero.config;

import com.slack.api.Slack;
import com.slack.api.methods.AsyncMethodsClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;

@Configuration
public class TestConfig {
    public final static LocalDate NOW = LocalDate.of(2020, Month.JUNE, 30);

    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasenames("messages");
        return source;
    }

    @Bean
    public Slack slack() {
        return new Slack();
    }

    @Bean
    public AsyncMethodsClient asyncMethodsClient(Slack slack) {
        return slack.methodsAsync();
    }

    @Bean
    public Clock clock() {
        return Clock.fixed(NOW.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
    }
}
