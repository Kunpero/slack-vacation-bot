package rs.kunpero.vacation.config;

import com.slack.api.Slack;
import com.slack.api.methods.AsyncMethodsClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
public class TestConfig {
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
}
