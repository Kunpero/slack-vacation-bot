package rs.kunpero.vacation.config.filter;

import com.slack.api.app_backend.SlackSignature;
import com.slack.api.app_backend.events.servlet.SlackSignatureVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

@Slf4j
@RequiredArgsConstructor
public class SlackRequestVerifierFilter extends GenericFilterBean {

    private final SlackSignatureVerifier slackSignatureVerifier;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        AuthenticationRequestWrapper wrappedRequest = new AuthenticationRequestWrapper((HttpServletRequest) servletRequest);
        String body = wrappedRequest.getReader().lines().collect(Collectors.joining());
        boolean isValid = slackSignatureVerifier.isValid(wrappedRequest, body);
        if (!isValid) { // invalid signature
            String signature = ((HttpServletRequest) servletRequest).getHeader(SlackSignature.HeaderNames.X_SLACK_SIGNATURE);
            log.debug("An invalid X-Slack-Signature detected - {}", signature);
            ((HttpServletResponse) servletResponse).setStatus(SC_UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(wrappedRequest, servletResponse);
    }
}
