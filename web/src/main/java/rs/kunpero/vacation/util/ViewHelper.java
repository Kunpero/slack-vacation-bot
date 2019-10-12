package rs.kunpero.vacation.util;

import com.github.seratch.jslack.api.model.block.ActionsBlock;
import com.github.seratch.jslack.api.model.block.DividerBlock;
import com.github.seratch.jslack.api.model.block.InputBlock;
import com.github.seratch.jslack.api.model.block.LayoutBlock;
import com.github.seratch.jslack.api.model.block.SectionBlock;
import com.github.seratch.jslack.api.model.block.composition.MarkdownTextObject;
import com.github.seratch.jslack.api.model.block.composition.PlainTextObject;
import com.github.seratch.jslack.api.model.block.element.ButtonElement;
import com.github.seratch.jslack.api.model.block.element.DatePickerElement;
import com.github.seratch.jslack.api.model.block.element.MultiUsersSelectElement;
import com.github.seratch.jslack.api.model.view.View;
import com.github.seratch.jslack.api.model.view.ViewSubmit;
import com.github.seratch.jslack.api.model.view.ViewTitle;
import com.github.seratch.jslack.app_backend.slash_commands.response.SlashCommandResponse;

import java.util.List;
import java.util.stream.Collectors;

import static rs.kunpero.vacation.util.ActionId.ADD_VACATION;
import static rs.kunpero.vacation.util.ActionId.CLOSE_DIALOG;
import static rs.kunpero.vacation.util.ActionId.SET_FROM;
import static rs.kunpero.vacation.util.ActionId.SET_SUBSTITUTION;
import static rs.kunpero.vacation.util.ActionId.SET_TO;
import static rs.kunpero.vacation.util.ActionId.SHOW_VACATION;
import static rs.kunpero.vacation.util.BlockId.DATE_FROM;
import static rs.kunpero.vacation.util.BlockId.DATE_TO;
import static rs.kunpero.vacation.util.BlockId.SUBSTITUTION;

public class ViewHelper {
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
                                                    .text("Show/Delete Vacation Info")
                                                    .emoji(true)
                                                    .build())
                                            .actionId(SHOW_VACATION.name()).build()))
                            .build()))
            .build();

    public static final View ADD_VACATION_INFO_VIEW = View.builder()
            .type("modal")
            .callbackId("callback_id")
            .title(ViewTitle.builder().type("plain_text").text("New vacation info").build())
            .submit(ViewSubmit.builder().type("plain_text").text("Submit").build())
            .notifyOnClose(true)
            .blocks(List.of(
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
                            .build()))
            .build();

    public static List<LayoutBlock> buildShowVacationBlocks(List<String> vacationInfoList) {
        List<LayoutBlock> blocks = vacationInfoList.stream()
                .map(v -> SectionBlock.builder()
                        .text(MarkdownTextObject.builder()
                                .text(v)
                                .build())
                        .accessory(ButtonElement.builder()
                                .style("danger")
                                .actionId("DELETE")
                                .text(PlainTextObject.builder()
                                        .text("Delete")
                                        .build())
                                .build())
                        .build())
                .collect(Collectors.toList());
        blocks.add(0, DividerBlock.builder().build());
        blocks.add(0, SectionBlock.builder()
                .text(MarkdownTextObject.builder()
                        .text(":camping: Your current vacation info:")
                        .build())
                .build());
        blocks.add(DividerBlock.builder().build());
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
}
