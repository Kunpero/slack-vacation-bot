package rs.kunpero.vacation.config;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.app_backend.SlackSignature;
import com.github.seratch.jslack.app_backend.events.servlet.SlackSignatureVerifier;
import com.github.seratch.jslack.app_backend.interactive_messages.ActionResponseSender;
import com.github.seratch.jslack.app_backend.interactive_messages.ResponseSender;
import com.github.seratch.jslack.common.json.GsonFactory;
import com.google.gson.Gson;
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
        return new Slack();
    }

    @Bean
    public ResponseSender responseSender(Slack slack) {
        return new ResponseSender(slack);
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
