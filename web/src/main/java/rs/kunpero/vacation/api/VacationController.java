package rs.kunpero.vacation.api;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.app_backend.interactive_messages.payload.BlockActionPayload;
import com.github.seratch.jslack.app_backend.interactive_messages.payload.PayloadTypeDetector;
import com.github.seratch.jslack.app_backend.slash_commands.response.SlashCommandResponse;
import com.github.seratch.jslack.app_backend.views.payload.ViewSubmissionPayload;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import rs.kunpero.vacation.api.dto.FormActivity;
import rs.kunpero.vacation.service.VacationService;
import rs.kunpero.vacation.service.dto.AddVacationInfoRequestDto;
import rs.kunpero.vacation.service.dto.AddVacationInfoResponseDto;
import rs.kunpero.vacation.util.ActionId;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static rs.kunpero.vacation.util.ActionId.ADD_VACATION;
import static rs.kunpero.vacation.util.ActionId.SET_FROM;
import static rs.kunpero.vacation.util.ActionId.SET_SUBSTITUTION;
import static rs.kunpero.vacation.util.ActionId.SET_TO;
import static rs.kunpero.vacation.util.ViewHelper.ADD_VACATION_INFO_VIEW;
import static rs.kunpero.vacation.util.ViewHelper.START_MENU;

@RestController
@Slf4j
@RequestMapping("/vacation")
public class VacationController {
    private static final ConcurrentMap<String, FormActivity> activityCache = new ConcurrentHashMap<>();
    private static final PayloadTypeDetector TYPE_DETECTOR = new PayloadTypeDetector();

    @Autowired
    private VacationService vacationService;
    @Autowired
    private Gson gson;
    @Autowired
    private Slack slack;

    @Value("${slack.access.token}")
    private String accessToken;

    @RequestMapping(value = "/start", method = RequestMethod.POST, consumes = APPLICATION_FORM_URLENCODED_VALUE, produces = APPLICATION_JSON_UTF8_VALUE)
    public SlashCommandResponse start() {
        return START_MENU;
    }

    @RequestMapping(value = "/interactivity", method = RequestMethod.POST, consumes = APPLICATION_FORM_URLENCODED_VALUE)
    public void interactivity(HttpServletRequest request, HttpServletResponse response) throws IOException, SlackApiException {
        String body = request.getReader().lines().collect(Collectors.joining());
        body = URLDecoder.decode(body, StandardCharsets.UTF_8)
                .replaceFirst("payload=", "");

        String type = TYPE_DETECTOR.detectType(body);
        if ("block_actions".equals(type)) {
            BlockActionPayload payload = gson.fromJson(body, BlockActionPayload.class);
            List<BlockActionPayload.Action> actions = payload.getActions();
            ActionId actionId = ActionId.safeValueOf(actions.get(0).getActionId());

            if (actionId == ADD_VACATION) {
                slack.methods(accessToken).viewsOpen(req -> req
                        .view(ADD_VACATION_INFO_VIEW)
                        .triggerId(payload.getTriggerId()));
            }
            if (actionId == SET_FROM) {
                String viewId = payload.getView().getId();
                FormActivity formActivity = activityCache.getOrDefault(viewId, new FormActivity());

                if (validateDate(LocalDate.parse(actions.get(0).getSelectedDate()), formActivity.getDateTo())) {

                }

                fillActivityCache(viewId, actions.get(0).getSelectedDate(),
                        (form, value) -> form.setDateFrom(LocalDate.parse(value)));
            }
            if (actionId == SET_TO) {
                String viewId = payload.getView().getId();
                FormActivity formActivity = activityCache.getOrDefault(viewId, new FormActivity());

                if (validateDate(formActivity.getDateFrom(), LocalDate.parse(actions.get(0).getSelectedDate()))) {

                }

                fillActivityCache(viewId, actions.get(0).getSelectedDate(),
                        (form, value) -> form.setDateTo(LocalDate.parse(value)));
            }
            if (actionId == SET_SUBSTITUTION) {
                String viewId = payload.getView().getId();
                fillActivityCache(viewId, actions.get(0).getSelectedUsers(),
                        FormActivity::setSubstitution);
            }
        }

        if ("view_submission".equals(type)) {
            ViewSubmissionPayload payload = gson.fromJson(body, ViewSubmissionPayload.class);
            log.info(activityCache.toString());
            /** maybe need to close if error occur. conduct research*/
            FormActivity formActivity = activityCache.get(payload.getView().getId());
            AddVacationInfoRequestDto requestDto = new AddVacationInfoRequestDto()
                    .setUserId(payload.getUser().getId())
                    .setDateFrom(formActivity.getDateFrom())
                    .setDateTo(formActivity.getDateTo())
                    .setSubstitutionIdList(formActivity.getSubstitution());

            AddVacationInfoResponseDto responseDto = vacationService.addVacationInfo(requestDto);
            if (responseDto.isSuccesful()) {
                activityCache.remove(payload.getView().getId());
            }

            // TODO: return error to slack
        }

        if ("view_closed".equals(type)) {
            ViewSubmissionPayload payload = gson.fromJson(body, ViewSubmissionPayload.class);
            activityCache.remove(payload.getView().getId());
        }

        response.setStatus(HttpServletResponse.SC_OK);
    }

    private boolean validateDate(LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom == null || dateTo == null) {
            return false;
        }
        return dateFrom.isBefore(dateTo);
    }

    private <T> void fillActivityCache(String viewId, T value, BiConsumer<FormActivity, T> fieldConsumer) {
        log.trace("ViewId={}", viewId);
        FormActivity formActivity = activityCache.getOrDefault(viewId, new FormActivity());
        fieldConsumer.accept(formActivity, value);
        activityCache.put(viewId, formActivity);
    }
}
