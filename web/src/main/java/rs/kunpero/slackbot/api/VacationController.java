package rs.kunpero.slackbot.api;

import com.google.gson.Gson;
import com.slack.api.app_backend.dialogs.payload.PayloadTypeDetector;
import com.slack.api.app_backend.interactive_components.ActionResponseSender;
import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload;
import com.slack.api.app_backend.interactive_components.response.ActionResponse;
import com.slack.api.app_backend.slash_commands.SlashCommandPayloadParser;
import com.slack.api.app_backend.slash_commands.payload.SlashCommandPayload;
import com.slack.api.app_backend.slash_commands.response.SlashCommandResponse;
import com.slack.api.app_backend.views.payload.ViewSubmissionPayload;
import com.slack.api.app_backend.views.response.ViewSubmissionResponse;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.view.View;
import com.slack.api.model.view.ViewState;
import com.slack.api.webhook.WebhookResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import rs.kunpero.slackbot.vacation.service.VacationAdminService;
import rs.kunpero.slackbot.vacation.service.VacationService;
import rs.kunpero.slackbot.vacation.service.dto.AddVacationInfoRequestDto;
import rs.kunpero.slackbot.vacation.service.dto.AddVacationInfoResponseDto;
import rs.kunpero.slackbot.vacation.service.dto.DeleteVacationInfoRequestDto;
import rs.kunpero.slackbot.vacation.service.dto.DeleteVacationInfoResponseDto;
import rs.kunpero.slackbot.vacation.service.dto.ShowVacationInfoRequestDto;
import rs.kunpero.slackbot.vacation.service.dto.ShowVacationInfoResponseDto;
import rs.kunpero.slackbot.vacation.util.ActionId;
import rs.kunpero.slackbot.vacation.util.BlockId;

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
import static rs.kunpero.slackbot.vacation.util.ActionId.ADD_VACATION;
import static rs.kunpero.slackbot.vacation.util.ActionId.CLOSE_DIALOG;
import static rs.kunpero.slackbot.vacation.util.ActionId.DELETE_VACATION;
import static rs.kunpero.slackbot.vacation.util.ActionId.SET_COMMENT;
import static rs.kunpero.slackbot.vacation.util.ActionId.SET_FROM;
import static rs.kunpero.slackbot.vacation.util.ActionId.SET_SUBSTITUTION;
import static rs.kunpero.slackbot.vacation.util.ActionId.SET_TO;
import static rs.kunpero.slackbot.vacation.util.ActionId.SET_VACATION_USER;
import static rs.kunpero.slackbot.vacation.util.ActionId.SHOW_VACATION;
import static rs.kunpero.slackbot.vacation.util.BlockId.COMMENT;
import static rs.kunpero.slackbot.vacation.util.BlockId.DATE_FROM;
import static rs.kunpero.slackbot.vacation.util.BlockId.DATE_TO;
import static rs.kunpero.slackbot.vacation.util.BlockId.ERROR;
import static rs.kunpero.slackbot.vacation.util.BlockId.SUBSTITUTION;
import static rs.kunpero.slackbot.vacation.util.BlockId.VACATION_USER;
import static rs.kunpero.slackbot.vacation.util.ViewHelperUtils.START_MENU;
import static rs.kunpero.slackbot.vacation.util.ViewHelperUtils.buildAddVacationInfoView;
import static rs.kunpero.slackbot.vacation.util.ViewHelperUtils.buildChatPostEphemeralRequest;
import static rs.kunpero.slackbot.vacation.util.ViewHelperUtils.buildUserShowVacationBlocks;
import static rs.kunpero.slackbot.vacation.util.ViewHelperUtils.buildVacationInfoView;

@RestController
@Slf4j
@RequestMapping("/vacation")
public class VacationController {
    private static final PayloadTypeDetector TYPE_DETECTOR = new PayloadTypeDetector();
    private static final SlashCommandPayloadParser SLASH_COMMAND_PAYLOAD_PARSER = new SlashCommandPayloadParser();

    private static final String BLOCK_ACTIONS_TYPE = "block_actions";
    private static final String VIEW_SUBMISSION_TYPE = "view_submission";

    private final VacationService vacationService;
    private final VacationAdminService vacationAdminService;
    private final Gson gson;
    private final MethodsClient methodsClient;
    private final ActionResponseSender actionResponseSender;

    private final String accessToken;

    @Autowired
    public VacationController(VacationService vacationService, VacationAdminService vacationAdminService, Gson gson,
                              MethodsClient methodsClient, ActionResponseSender actionResponseSender,
                              @Value("${slack.access.token}") String accessToken) {
        this.vacationService = vacationService;
        this.vacationAdminService = vacationAdminService;
        this.gson = gson;
        this.methodsClient = methodsClient;
        this.actionResponseSender = actionResponseSender;
        this.accessToken = accessToken;
    }

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
        final boolean isAdmin = vacationAdminService.isAdmin(payload.getUser().getId(), payload.getUser().getTeamId());
        if (actionId == ADD_VACATION) {

            methodsClient.viewsOpen(req -> req
                    .view(buildAddVacationInfoView(payload.getChannel().getId(), payload.getUser().getId(), isAdmin))
                    .triggerId(payload.getTriggerId()));

            actionResponse = ActionResponse.builder()
                    .deleteOriginal(true)
                    .responseType("ephemeral")
                    .build();
        } else if (actionId == SHOW_VACATION) {
            ShowVacationInfoRequestDto requestDto = new ShowVacationInfoRequestDto()
                    .setUserId(payload.getUser().getId())
                    .setTeamId(payload.getUser().getTeamId())
                    .setAdmin(isAdmin);
            ShowVacationInfoResponseDto responseDto = vacationService.showVacationInfo(requestDto);

            List<LayoutBlock> blocks = buildUserShowVacationBlocks(responseDto.getVacationInfoList());
            actionResponse = ActionResponse.builder()
                    .replaceOriginal(true)
                    .responseType("ephemeral")
                    .blocks(blocks)
                    .build();
        } else if (actionId == DELETE_VACATION) {
            DeleteVacationInfoRequestDto requestDto = new DeleteVacationInfoRequestDto()
                    .setVacationInfoId(Long.valueOf(actions.get(0).getValue()))
                    .setUserId(payload.getUser().getId())
                    .setTeamId(payload.getUser().getTeamId())
                    .setAdmin(isAdmin);
            DeleteVacationInfoResponseDto responseDto = vacationService.deleteVacationInfo(requestDto);

            List<LayoutBlock> blocks = buildUserShowVacationBlocks(responseDto.getVacationInfoList());
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

        Map<String, ViewState.Value> vacationUserMap = valuesMap.get(VACATION_USER.name());

        String userId;
        if (CollectionUtils.isEmpty(vacationUserMap)) {
            userId = payload.getUser().getId();
        } else {
            userId = vacationUserMap.get(SET_VACATION_USER.name()).getSelectedUser();
        }
        AddVacationInfoRequestDto requestDto = new AddVacationInfoRequestDto()
                .setUserId(userId)
                .setTeamId(payload.getUser().getTeamId())
                .setDateFrom(LocalDate.parse(valuesMap.get(DATE_FROM.name()).get(SET_FROM.name()).getSelectedDate()))
                .setDateTo(LocalDate.parse(valuesMap.get(DATE_TO.name()).get(SET_TO.name()).getSelectedDate()))
                .setSubstitutionIdList(valuesMap.get(SUBSTITUTION.name()).get(SET_SUBSTITUTION.name()).getSelectedUsers())
                .setComment(valuesMap.get(COMMENT.name()).get(SET_COMMENT.name()).getValue());
        AddVacationInfoResponseDto responseDto = vacationService.addVacationInfo(requestDto);

        if (responseDto.isSuccessful()) {
            methodsClient
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
                        .text(errorDescription)
                        .build()).build());

        boolean isAdmin = vacationAdminService.isAdmin(payload.getUser().getId(), payload.getUser().getTeamId());
        View viewWithError = buildAddVacationInfoView(payload.getView().getCallbackId(), payload.getUser().getId(), isAdmin);
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