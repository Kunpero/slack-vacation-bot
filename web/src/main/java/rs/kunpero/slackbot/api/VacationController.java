package rs.kunpero.slackbot.api;

import com.slack.api.app_backend.slash_commands.payload.SlashCommandPayload;
import com.slack.api.app_backend.slash_commands.response.SlashCommandResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import rs.kunpero.slackbot.vacation.service.VacationService;
import rs.kunpero.slackbot.vacation.service.dto.ShowVacationInfoResponseDto;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static rs.kunpero.slackbot.config.SlackConfig.SLASH_COMMAND_PAYLOAD_PARSER;
import static rs.kunpero.slackbot.vacation.util.ViewHelperUtils.VACATION_START_VIEW;
import static rs.kunpero.slackbot.vacation.util.ViewHelperUtils.buildVacationInfoView;

@RestController
@Slf4j
@RequestMapping("/vacation")
@RequiredArgsConstructor
public class VacationController {
    private final VacationService vacationService;

    @RequestMapping(value = "/start", method = RequestMethod.POST, consumes = APPLICATION_FORM_URLENCODED_VALUE, produces = APPLICATION_JSON_UTF8_VALUE)
    public SlashCommandResponse start(HttpServletRequest request) throws IOException {
        String body = request.getReader().lines().collect(Collectors.joining());
        body = URLDecoder.decode(body, StandardCharsets.UTF_8);

        SlashCommandPayload payload = SLASH_COMMAND_PAYLOAD_PARSER.parse(body);

        if (!StringUtils.isEmpty(payload.getText()) && "now".equals(payload.getText())) {
            ShowVacationInfoResponseDto responseDto = vacationService.showCurrentDayVacationInfo(payload.getTeamId());
            return buildVacationInfoView(responseDto.getVacationInfoList(), "Vacation info for current date:");
        }

        if (!StringUtils.isEmpty(payload.getText()) && "all".equals(payload.getText())) {
            ShowVacationInfoResponseDto responseDto = vacationService.showAllActualVacations(payload.getTeamId());
            return buildVacationInfoView(responseDto.getVacationInfoList(), "Upcoming vacations:");
        }

        return VACATION_START_VIEW;
    }
}