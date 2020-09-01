package rs.kunpero.vacation.config;

import com.google.gson.Gson;
import com.slack.api.Slack;
import com.slack.api.app_backend.SlackSignature;
import com.slack.api.app_backend.events.servlet.SlackSignatureVerifier;
import com.slack.api.app_backend.interactive_components.ActionResponseSender;
import com.slack.api.util.json.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.GsonHttpMessageConverter;

@Configuration
public class SlackConfig {

    @Value("${slack.signing.secret}")
    private String signingSecret;

    @Bean
    public Slack slack() {
        return Slack.getInstance();
    }

    @Bean
    public ActionResponseSender responseSender(Slack slack) {
        return new ActionResponseSender(slack);
    }

    @Bean
    public GsonHttpMessageConverter gsonHttpMessageConverter(Gson gson) {
        GsonHttpMessageConverter converter = new GsonHttpMessageConverter();
        converter.setGson(gson);
        return converter;
    }

    @Bean
    public Gson gson() {
        return GsonFactory.createSnakeCase();
    }

    @Bean
    public SlackSignature.Generator slackSignatureGenerator() {
        return new SlackSignature.Generator(signingSecret);
    }

    @Bean
    public SlackSignatureVerifier slackSignatureVerifier(SlackSignature.Generator slackSignatureGenerator) {
        return new SlackSignatureVerifier(slackSignatureGenerator);
    }

    @Bean
    public ActionResponseSender actionResponseSender(Slack slack) {
        return new ActionResponseSender(slack);
    }
}
