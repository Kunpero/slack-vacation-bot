package rs.kunpero.slackbot.vacation.util;

import com.slack.api.app_backend.slash_commands.response.SlashCommandResponse;
import com.slack.api.methods.request.chat.ChatPostEphemeralRequest;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.model.block.ActionsBlock;
import com.slack.api.model.block.DividerBlock;
import com.slack.api.model.block.InputBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.BlockElement;
import com.slack.api.model.block.element.ButtonElement;
import com.slack.api.model.block.element.DatePickerElement;
import com.slack.api.model.block.element.MultiUsersSelectElement;
import com.slack.api.model.block.element.PlainTextInputElement;
import com.slack.api.model.block.element.UsersSelectElement;
import com.slack.api.model.view.View;
import com.slack.api.model.view.ViewSubmit;
import com.slack.api.model.view.ViewTitle;
import rs.kunpero.slackbot.vacation.service.dto.VacationInfoDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static rs.kunpero.slackbot.vacation.service.VacationService.COMMENT_MAX_LENGTH;
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
import static rs.kunpero.slackbot.vacation.util.BlockId.SUBSTITUTION;
import static rs.kunpero.slackbot.vacation.util.BlockId.VACATION_USER;

public class ViewHelperUtils {
    private static final List<BlockElement> MAIN_BUTTONS = List.of(
            ButtonElement.builder()
                    .style("primary")
                    .text(PlainTextObject.builder()
                            .text("Add Vacation")
                            .emoji(true)
                            .build())
                    .actionId(ADD_VACATION.name())
                    .build(),
            ButtonElement.builder()
                    .text(PlainTextObject.builder()
                            .text("Show/Delete Vacation Info")
                            .emoji(true)
                            .build())
                    .actionId(SHOW_VACATION.name())
                    .build(),
            ButtonElement.builder()
                    .text(PlainTextObject.builder()
                            .text("Close")
                            .emoji(true)
                            .build())
                    .actionId(CLOSE_DIALOG.name())
                    .build()
    );

    public static final SlashCommandResponse START_MENU = SlashCommandResponse.builder()
            .responseType("ephemeral")
            .blocks(List.of(
                    SectionBlock.builder()
                            .text(MarkdownTextObject.builder()
                                    .text(":beach_with_umbrella: Hello, this is vacation helper! \n\n *Please choose action:*\n")
                                    .build())
                            .build(),
                    DividerBlock.builder().build(),
                    ActionsBlock.builder()
                            .blockId(ADD_VACATION.name())
                            .elements(MAIN_BUTTONS)
                            .build()))
            .build();

    public static View buildAddVacationInfoView(String channelId, String userId, boolean isAdmin) {
        List<LayoutBlock> blocks = new ArrayList<>();
        if (isAdmin) {
            InputBlock userSelector = InputBlock.builder()
                    .blockId(VACATION_USER.name())
                    .optional(false)
                    .label(PlainTextObject.builder()
                            .text("Username")
                            .build())
                    .element(UsersSelectElement.builder()
                            .actionId(SET_VACATION_USER.name())
                            .initialUser(userId)
                            .placeholder(PlainTextObject.builder()
                                    .text("Select user")
                                    .build())
                            .build())
                    .build();
            blocks.add(userSelector);
        }
        blocks.addAll(List.of(
                InputBlock.builder()
                        .blockId(DATE_FROM.name())
                        .optional(false)
                        .label(PlainTextObject.builder()
                                .text("Date from")
                                .emoji(true)
                                .build())
                        .element(DatePickerElement.builder()
                                .actionId(SET_FROM.name())
                                .placeholder(PlainTextObject.builder()
                                        .text("Select a date")
                                        .build())
                                .build())
                        .build(),
                InputBlock.builder()
                        .blockId(DATE_TO.name())
                        .optional(false)
                        .label(PlainTextObject.builder()
                                .text("Date to")
                                .emoji(true)
                                .build())
                        .element(DatePickerElement.builder()
                                .actionId(SET_TO.name())
                                .placeholder(PlainTextObject.builder()
                                        .text("Select a date")
                                        .build())
                                .build())
                        .build(),
                InputBlock.builder()
                        .blockId(SUBSTITUTION.name())
                        .optional(true)
                        .label(PlainTextObject.builder()
                                .text("Substitution")
                                .emoji(true)
                                .build())
                        .element(MultiUsersSelectElement.builder()
                                .actionId(SET_SUBSTITUTION.name())
                                .placeholder(PlainTextObject.builder()
                                        .text("Choose your substitution")
                                        .build())
                                .build())
                        .build(),
                InputBlock.builder()
                        .blockId(COMMENT.name())
                        .optional(true)
                        .label(PlainTextObject.builder()
                                .text("Comment")
                                .emoji(true)
                                .build())
                        .element(PlainTextInputElement.builder()
                                .actionId(SET_COMMENT.name())
                                .maxLength(COMMENT_MAX_LENGTH)
                                .multiline(true)
                                .placeholder(PlainTextObject.builder()
                                        .text("Write down your commentary (512 symbols)")
                                        .build())
                                .build())
                        .build()));

        return View.builder()
                .type("modal")
                .callbackId(channelId)
                .title(ViewTitle.builder().type("plain_text").text("New vacation info").build())
                .submit(ViewSubmit.builder().type("plain_text").text("Submit").build())
                .notifyOnClose(true)
                .blocks(blocks)
                .build();
    }

    public static SlashCommandResponse buildVacationInfoView(List<VacationInfoDto> vacationInfoList, String message) {

        List<LayoutBlock> blocks = new ArrayList<>();
        blocks.add(SectionBlock.builder()
                .text(MarkdownTextObject.builder()
                        .text(":notebook_with_decorative_cover: " + message)
                        .build())
                .build());
        if (!vacationInfoList.isEmpty()) {
            blocks.addAll(vacationInfoList.stream()
                    .map(v -> SectionBlock.builder()
                            .text(MarkdownTextObject.builder()
                                    .text(v.getVacationInfo())
                                    .build())
                            .build())
                    .collect(Collectors.toList()));
        } else {
            blocks.add(SectionBlock.builder()
                    .text(MarkdownTextObject.builder()
                            .text("No active vacations :white_frowning_face:")
                            .build())
                    .build());
        }
        blocks.add(ActionsBlock.builder()
                .elements(List.of(ButtonElement.builder()
                        .text(PlainTextObject.builder()
                                .text("Close")
                                .emoji(true)
                                .build())
                        .actionId(CLOSE_DIALOG.name())
                        .build()))
                .build());
        return SlashCommandResponse.builder()
                .responseType("ephemeral")
                .blocks(blocks)
                .build();
    }

    public static List<LayoutBlock> buildUserShowVacationBlocks(List<VacationInfoDto> vacationInfoList) {
        List<LayoutBlock> blocks = new ArrayList<>();
        if (vacationInfoList.isEmpty()) {
            blocks.add(SectionBlock.builder()
                    .text(MarkdownTextObject.builder()
                            .text("You have no vacations yet :white_frowning_face:")
                            .build())
                    .build());
        } else {
            blocks.addAll(vacationInfoList.stream()
                    .map(v -> SectionBlock.builder()
                            .text(MarkdownTextObject.builder()
                                    .text(v.getVacationInfo())
                                    .build())
                            .accessory(ButtonElement.builder()
                                    .style("danger")
                                    .actionId(DELETE_VACATION.name())
                                    .value(String.valueOf(v.getVacationId()))
                                    .text(PlainTextObject.builder()
                                            .text("Delete")
                                            .build())
                                    .build())
                            .build())
                    .collect(Collectors.toList()));
            blocks.add(0, DividerBlock.builder().build());
            blocks.add(0, SectionBlock.builder()
                    .text(MarkdownTextObject.builder()
                            .text(":camping: Your current vacation info:")
                            .build())
                    .build());
            blocks.add(DividerBlock.builder().build());
        }
        blocks.add(ActionsBlock.builder()
                .blockId(ADD_VACATION.name())
                .elements(List.of(
                        ButtonElement.builder()
                                .style("primary")
                                .text(PlainTextObject.builder()
                                        .text("Add Vacation")
                                        .emoji(true)
                                        .build())
                                .actionId(ADD_VACATION.name())
                                .build(),
                        ButtonElement.builder()
                                .text(PlainTextObject.builder()
                                        .text("Close")
                                        .emoji(true)
                                        .build())
                                .actionId(CLOSE_DIALOG.name()).build()))
                .build());
        return blocks;
    }

    public static ChatPostEphemeralRequest buildChatPostEphemeralRequest(String userId, String callbackId) {
        return ChatPostEphemeralRequest.builder()
                .user(userId)
                .text("Success")
                .channel(callbackId)
                .blocks(List.of(SectionBlock.builder()
                                .text(MarkdownTextObject.builder()
                                        .text("New vacation info was successfully saved :desert_island:")
                                        .build())
                                .build(),
                        ActionsBlock.builder()
                                .blockId(ADD_VACATION.name())
                                .elements(MAIN_BUTTONS)
                                .build()))
                .build();
    }

    public static ChatPostMessageRequest buildChatPostRequest(String channelId,
                                                              List<VacationInfoDto> vacationInfoList) {
        return ChatPostMessageRequest.builder()
                .channel(channelId)
                .blocks(buildChannelShowVacationBlocks(vacationInfoList))
                .build();
    }

    public static ChatPostMessageRequest buildDutyNotifyPostRequest(String channelId, String userId) {
        return ChatPostMessageRequest.builder()
                .channel(channelId)
                .blocks(List.of(SectionBlock.builder()
                        .text(MarkdownTextObject.builder()
                                .text(String.format("<@%s> is on duty today! :zap:", userId))
                                .build())
                        .build()))
                .build();
    }


    private static List<LayoutBlock> buildChannelShowVacationBlocks(List<VacationInfoDto> vacationInfoList) {
        List<LayoutBlock> blocks = new ArrayList<>();
        if (vacationInfoList.isEmpty()) {
            blocks.add(SectionBlock.builder()
                    .text(MarkdownTextObject.builder()
                            .text("No vacations yet :white_frowning_face:")
                            .build())
                    .build());
        } else {
            blocks.addAll(vacationInfoList.stream()
                    .map(v -> SectionBlock.builder()
                            .text(MarkdownTextObject.builder()
                                    .text(v.getVacationInfo())
                                    .build())
                            .build())
                    .collect(Collectors.toList()));
            blocks.add(0, DividerBlock.builder().build());
            blocks.add(0, SectionBlock.builder()
                    .text(MarkdownTextObject.builder()
                            .text(":umbrella_on_ground: Actual vacation info:")
                            .build())
                    .build());
            blocks.add(DividerBlock.builder().build());
        }
        return blocks;
    }
}
