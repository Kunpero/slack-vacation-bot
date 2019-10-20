package rs.kunpero.vacation.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class MessageSourceHelper {
    private final MessageSource messageSource;

    @Autowired
    public MessageSourceHelper(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Value("${default.locale}")
    private String defaultLocale;

    public int getCode(String source) {
        String code = messageSource.getMessage(source + ".code", null, new Locale(defaultLocale));
        return Integer.parseInt(code);
    }

    public String getMessage(String source, @Nullable String[] args) {
        return messageSource.getMessage(source + ".message", args, new Locale(defaultLocale));
    }
}
