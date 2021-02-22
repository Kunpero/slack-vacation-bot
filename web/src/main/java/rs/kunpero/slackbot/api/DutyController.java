package rs.kunpero.slackbot.api;

import com.slack.api.app_backend.slash_commands.payload.SlashCommandPayload;
import com.slack.api.app_backend.slash_commands.response.SlashCommandResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import rs.kunpero.slackbot.dutycarousel.entity.DutyList;
import rs.kunpero.slackbot.dutycarousel.service.DutyService;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static rs.kunpero.slackbot.config.SlackConfig.SLASH_COMMAND_PAYLOAD_PARSER;
import static rs.kunpero.slackbot.vacation.util.ViewHelperUtils.DUTY_START_ERROR_VIEW;
import static rs.kunpero.slackbot.vacation.util.ViewHelperUtils.DUTY_START_VIEW;

@RestController
@Slf4j
@RequestMapping("/duty")
@RequiredArgsConstructor
public class DutyController {
    private final DutyService dutyService;

    @RequestMapping(value = "/start", method = RequestMethod.POST, consumes = APPLICATION_FORM_URLENCODED_VALUE, produces = APPLICATION_JSON_UTF8_VALUE)
    public SlashCommandResponse start(HttpServletRequest request) throws IOException {
        String body = request.getReader().lines().collect(Collectors.joining());
        body = URLDecoder.decode(body, StandardCharsets.UTF_8);

        SlashCommandPayload payload = SLASH_COMMAND_PAYLOAD_PARSER.parse(body);
        String channelId = payload.getChannelId();
        DutyList dutyList = dutyService.findDutyList(channelId);
        if (dutyList != null) {
            return DUTY_START_VIEW;
        } else {
            return DUTY_START_ERROR_VIEW;
        }
    }
}
