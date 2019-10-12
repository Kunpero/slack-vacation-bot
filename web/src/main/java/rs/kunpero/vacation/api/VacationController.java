package rs.kunpero.vacation.api;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.model.block.LayoutBlock;
import com.github.seratch.jslack.api.model.block.SectionBlock;
import com.github.seratch.jslack.api.model.block.composition.MarkdownTextObject;
import com.github.seratch.jslack.api.model.view.View;
import com.github.seratch.jslack.api.model.view.ViewState;
import com.github.seratch.jslack.api.model.view.ViewSubmit;
import com.github.seratch.jslack.api.model.view.ViewTitle;
import com.github.seratch.jslack.app_backend.interactive_messages.payload.BlockActionPayload;
import com.github.seratch.jslack.app_backend.interactive_messages.payload.PayloadTypeDetector;
import com.github.seratch.jslack.app_backend.slash_commands.response.SlashCommandResponse;
import com.github.seratch.jslack.app_backend.views.payload.ViewSubmissionPayload;
import com.github.seratch.jslack.app_backend.views.response.ViewSubmissionResponse;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import rs.kunpero.vacation.service.VacationService;
import rs.kunpero.vacation.service.dto.AddVacationInfoRequestDto;
import rs.kunpero.vacation.service.dto.AddVacationInfoResponseDto;
import rs.kunpero.vacation.util.ActionId;
import rs.kunpero.vacation.util.BlockId;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static rs.kunpero.vacation.util.ActionId.ADD_VACATION;
import static rs.kunpero.vacation.util.ActionId.SET_FROM;
import static rs.kunpero.vacation.util.ActionId.SET_SUBSTITUTION;
import static rs.kunpero.vacation.util.ActionId.SET_TO;
import static rs.kunpero.vacation.util.BlockId.DATE_FROM;
import static rs.kunpero.vacation.util.BlockId.DATE_TO;
import static rs.kunpero.vacation.util.BlockId.ERROR;
import static rs.kunpero.vacation.util.BlockId.SUBSTITUTION;
import static rs.kunpero.vacation.util.ViewHelper.ADD_VACATION_INFO_VIEW;
import static rs.kunpero.vacation.util.ViewHelper.START_MENU;

@RestController
@Slf4j
@RequestMapping("/vacation")
public class VacationController {
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

            response.setStatus(HttpServletResponse.SC_OK);
        } else if ("view_submission".equals(type)) {
            ViewSubmissionPayload payload = gson.fromJson(body, ViewSubmissionPayload.class);
            Map<String, Map<String, ViewState.Value>> valuesMap = payload.getView().getState().getValues();
            AddVacationInfoRequestDto requestDto = new AddVacationInfoRequestDto()
                    .setUserId(payload.getUser().getId())
                    .setTeamId(payload.getUser().getTeamId())
                    .setDateFrom(LocalDate.parse(valuesMap.get(DATE_FROM.name()).get(SET_FROM.name()).getSelectedDate()))
                    .setDateTo(LocalDate.parse(valuesMap.get(DATE_TO.name()).get(SET_TO.name()).getSelectedDate()))
                    .setSubstitutionIdList(valuesMap.get(SUBSTITUTION.name()).get(SET_SUBSTITUTION.name()).getSelectedUsers());
            AddVacationInfoResponseDto responseDto = vacationService.addVacationInfo(requestDto);
            if (!responseDto.isSuccesful()) {
                buildErrorResponse(payload, responseDto.getErrorDescription(), response);
            }
        }
    }

    private void buildErrorResponse(ViewSubmissionPayload payload, String errorDescription, HttpServletResponse response) throws IOException {
        List<LayoutBlock> blocks = payload.getView().getBlocks();
        if (blocks.size() == BlockId.values().length) {
            blocks.remove(BlockId.values().length - 1);
        }
        blocks.add(SectionBlock.builder()
                .blockId(ERROR.name())
                .text(MarkdownTextObject.builder()
                        .text(String.format("`%s`", errorDescription))
                        .build()).build());
        View viewWithError = View.builder()
                .type("modal")
                .callbackId("callback_id")
                .title(ViewTitle.builder().type("plain_text").text("New vacation info").build())
                .submit(ViewSubmit.builder().type("plain_text").text("Submit").build())
                .notifyOnClose(true)
                .blocks(blocks)
                .build();
        ViewSubmissionResponse submissionResponse = ViewSubmissionResponse.builder()
                .responseAction("update")
                .view(viewWithError)
                .build();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(submissionResponse));
        response.getWriter().flush();
    }
}