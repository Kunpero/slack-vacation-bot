package rs.kunpero.vacation.config;

import com.github.seratch.jslack.app_backend.events.servlet.SlackSignatureVerifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import rs.kunpero.vacation.config.filter.SlackRequestVerifierFilter;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .mvcMatchers("/vacation/**")
                .permitAll()
                .and()
                .requiresChannel()
                .requestMatchers(r -> r.getHeader("X-Forwarded-Proto") != null)
                .requiresSecure()
                .and().csrf().disable();
    }

    @Bean
    public SlackRequestVerifierFilter slackRequestVerifierFilter(SlackSignatureVerifier slackSignatureVerifier) {
        return new SlackRequestVerifierFilter(slackSignatureVerifier);
    }

    @Bean
    public FilterRegistrationBean slackRequestVerifierFilterRegistration(SlackRequestVerifierFilter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(filter);
        registration.addUrlPatterns("/vacation/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
