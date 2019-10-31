package rs.kunpero.vacation.api;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.model.block.LayoutBlock;
import com.github.seratch.jslack.api.model.block.SectionBlock;
import com.github.seratch.jslack.api.model.block.composition.MarkdownTextObject;
import com.github.seratch.jslack.api.model.view.View;
import com.github.seratch.jslack.api.model.view.ViewState;
import com.github.seratch.jslack.api.webhook.WebhookResponse;
import com.github.seratch.jslack.app_backend.interactive_messages.ActionResponseSender;
import com.github.seratch.jslack.app_backend.interactive_messages.payload.BlockActionPayload;
import com.github.seratch.jslack.app_backend.interactive_messages.payload.PayloadTypeDetector;
import com.github.seratch.jslack.app_backend.interactive_messages.response.ActionResponse;
import com.github.seratch.jslack.app_backend.slash_commands.payload.SlashCommandPayload;
import com.github.seratch.jslack.app_backend.slash_commands.payload.SlashCommandPayloadParser;
import com.github.seratch.jslack.app_backend.slash_commands.response.SlashCommandResponse;
import com.github.seratch.jslack.app_backend.views.payload.ViewSubmissionPayload;
import com.github.seratch.jslack.app_backend.views.response.ViewSubmissionResponse;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import rs.kunpero.vacation.service.VacationService;
import rs.kunpero.vacation.service.dto.AddVacationInfoRequestDto;
import rs.kunpero.vacation.service.dto.AddVacationInfoResponseDto;
import rs.kunpero.vacation.service.dto.DeleteVacationInfoRequestDto;
import rs.kunpero.vacation.service.dto.DeleteVacationInfoResponseDto;
import rs.kunpero.vacation.service.dto.ShowVacationInfoRequestDto;
import rs.kunpero.vacation.service.dto.ShowVacationInfoResponseDto;
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
import static rs.kunpero.vacation.util.ActionId.CLOSE_DIALOG;
import static rs.kunpero.vacation.util.ActionId.DELETE_VACATION;
import static rs.kunpero.vacation.util.ActionId.SET_FROM;
import static rs.kunpero.vacation.util.ActionId.SET_SUBSTITUTION;
import static rs.kunpero.vacation.util.ActionId.SET_TO;
import static rs.kunpero.vacation.util.ActionId.SHOW_VACATION;
import static rs.kunpero.vacation.util.BlockId.DATE_FROM;
import static rs.kunpero.vacation.util.BlockId.DATE_TO;
import static rs.kunpero.vacation.util.BlockId.ERROR;
import static rs.kunpero.vacation.util.BlockId.SUBSTITUTION;
import static rs.kunpero.vacation.util.ViewHelper.START_MENU;
import static rs.kunpero.vacation.util.ViewHelper.buildAddVacationInfoView;
import static rs.kunpero.vacation.util.ViewHelper.buildChatPostEphemeralRequest;
import static rs.kunpero.vacation.util.ViewHelper.buildCurrentDateVacationInfo;
import static rs.kunpero.vacation.util.ViewHelper.buildShowVacationBlocks;

@RestController
@Slf4j
@RequestMapping("/vacation")
public class VacationController {
    private static final PayloadTypeDetector TYPE_DETECTOR = new PayloadTypeDetector();
    private static final SlashCommandPayloadParser SLASH_COMMAND_PAYLOAD_PARSER = new SlashCommandPayloadParser();

    private static final String BLOCK_ACTIONS_TYPE = "block_actions";
    private static final String VIEW_SUBMISSION_TYPE = "view_submission";

    private final VacationService vacationService;
    private final Gson gson;
    private final Slack slack;
    private final ActionResponseSender actionResponseSender;

    @Autowired
    public VacationController(VacationService vacationService, Gson gson, Slack slack, ActionResponseSender actionResponseSender) {
        this.vacationService = vacationService;
        this.gson = gson;
        this.slack = slack;
        this.actionResponseSender = actionResponseSender;
    }

    @Value("${slack.access.token}")
    private String accessToken;

    @RequestMapping(value = "/start", method = RequestMethod.POST, consumes = APPLICATION_FORM_URLENCODED_VALUE, produces = APPLICATION_JSON_UTF8_VALUE)
    public SlashCommandResponse start(HttpServletRequest request) throws IOException {
        String body = request.getReader().lines().collect(Collectors.joining());
        body = URLDecoder.decode(body, StandardCharsets.UTF_8);

        SlashCommandPayload payload = SLASH_COMMAND_PAYLOAD_PARSER.parse(body);

        if (!StringUtils.isEmpty(payload.getText()) && "now".equals(payload.getText())) {
            ShowVacationInfoResponseDto responseDto = vacationService.showCurrentDayVacationInfo(payload.getTeamId());
            return buildCurrentDateVacationInfo(responseDto.getVacationInfoList());
        }

        if (!StringUtils.isEmpty(payload.getText()) && "all".equals(payload.getText())) {
            ShowVacationInfoResponseDto responseDto = vacationService.showAllActualVacations(payload.getTeamId());
            return buildCurrentDateVacationInfo(responseDto.getVacationInfoList());
        }
        return START_MENU;
    }

    @RequestMapping(value = "/interactivity", method = RequestMethod.POST, consumes = APPLICATION_FORM_URLENCODED_VALUE)
    public void interactivity(HttpServletRequest request, HttpServletResponse response) throws IOException, SlackApiException {
        String body = request.getReader().lines().collect(Collectors.joining());
        body = URLDecoder.decode(body, StandardCharsets.UTF_8)
                .replaceFirst("payload=", "");

        String type = TYPE_DETECTOR.detectType(body);
        if (BLOCK_ACTIONS_TYPE.equals(type)) {
            handleBlockActions(body);
        } else if (VIEW_SUBMISSION_TYPE.equals(type)) {
            handleViewSubmission(body, response);
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void handleBlockActions(String body) throws IOException, SlackApiException {
        BlockActionPayload payload = gson.fromJson(body, BlockActionPayload.class);
        List<BlockActionPayload.Action> actions = payload.getActions();
        ActionId actionId = ActionId.safeValueOf(actions.get(0).getActionId());

        ActionResponse actionResponse = null;
        if (actionId == ADD_VACATION) {
            slack.methods(accessToken).viewsOpen(req -> req
                    .view(buildAddVacationInfoView(payload.getChannel().getId()))
                    .triggerId(payload.getTriggerId()));

            actionResponse = ActionResponse.builder()
                    .deleteOriginal(true)
                    .responseType("ephemeral")
                    .build();
        } else if (actionId == SHOW_VACATION) {
            ShowVacationInfoRequestDto requestDto = new ShowVacationInfoRequestDto()
                    .setUserId(payload.getUser().getId())
                    .setTeamId(payload.getUser().getTeamId());
            ShowVacationInfoResponseDto responseDto = vacationService.showVacationInfo(requestDto);

            List<LayoutBlock> blocks = buildShowVacationBlocks(responseDto.getVacationInfoList());
            actionResponse = ActionResponse.builder()
                    .replaceOriginal(true)
                    .responseType("ephemeral")
                    .blocks(blocks)
                    .build();
        } else if (actionId == DELETE_VACATION) {
            DeleteVacationInfoRequestDto requestDto = new DeleteVacationInfoRequestDto()
                    .setVacationInfoId(Long.valueOf(actions.get(0).getValue()))
                    .setUserId(payload.getUser().getId())
                    .setTeamId(payload.getUser().getTeamId());
            DeleteVacationInfoResponseDto responseDto = vacationService.deleteVacationInfo(requestDto);

            List<LayoutBlock> blocks = buildShowVacationBlocks(responseDto.getVacationInfoList());
            actionResponse = ActionResponse.builder()
                    .replaceOriginal(true)
                    .responseType("ephemeral")
                    .blocks(blocks)
                    .build();

        } else if (actionId == CLOSE_DIALOG) {
            actionResponse = ActionResponse.builder()
                    .deleteOriginal(true)
                    .responseType("ephemeral")
                    .build();

        }

        WebhookResponse response = actionResponseSender.send(payload.getResponseUrl(), actionResponse);
        log.debug(response.toString());
    }

    private void handleViewSubmission(String body, HttpServletResponse response) throws IOException, SlackApiException {
        ViewSubmissionPayload payload = gson.fromJson(body, ViewSubmissionPayload.class);
        Map<String, Map<String, ViewState.Value>> valuesMap = payload.getView().getState().getValues();
        AddVacationInfoRequestDto requestDto = new AddVacationInfoRequestDto()
                .setUserId(payload.getUser().getId())
                .setTeamId(payload.getUser().getTeamId())
                .setDateFrom(LocalDate.parse(valuesMap.get(DATE_FROM.name()).get(SET_FROM.name()).getSelectedDate()))
                .setDateTo(LocalDate.parse(valuesMap.get(DATE_TO.name()).get(SET_TO.name()).getSelectedDate()))
                .setSubstitutionIdList(valuesMap.get(SUBSTITUTION.name()).get(SET_SUBSTITUTION.name()).getSelectedUsers());
        AddVacationInfoResponseDto responseDto = vacationService.addVacationInfo(requestDto);

        if (responseDto.isSuccessful()) {
            slack.methods(accessToken)
                    .chatPostEphemeral(buildChatPostEphemeralRequest(payload.getUser().getId(), accessToken, payload.getView().getCallbackId()));
            return;
        }
        buildErrorResponse(payload, responseDto.getErrorDescription(), response);
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
        View viewWithError = buildAddVacationInfoView(payload.getView().getCallbackId());
        viewWithError.setBlocks(blocks);
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