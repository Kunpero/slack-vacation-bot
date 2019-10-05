package rs.kunpero.vacation.config.filter;

import com.github.seratch.jslack.app_backend.SlackSignature;
import com.github.seratch.jslack.app_backend.events.servlet.SlackSignatureVerifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Component
@Order(HIGHEST_PRECEDENCE)
@Slf4j
public class SlackRequestVerifierFilter extends GenericFilterBean {
    @Autowired
    private SlackSignatureVerifier slackSignatureVerifier;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        AuthenticationRequestWrapper wrappedRequest = new AuthenticationRequestWrapper((HttpServletRequest) servletRequest);
        String body = new String(wrappedRequest.getRequestBody());
        boolean isValid = slackSignatureVerifier.isValid((HttpServletRequest) servletRequest, body);
        if (!isValid) { // invalid signature
            String signature = ((HttpServletRequest) servletRequest).getHeader(SlackSignature.HeaderNames.X_SLACK_SIGNATURE);
            log.debug("An invalid X-Slack-Signature detected - {}", signature);
            ((HttpServletResponse) servletResponse).setStatus(SC_UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(wrappedRequest, servletResponse);
    }
}
