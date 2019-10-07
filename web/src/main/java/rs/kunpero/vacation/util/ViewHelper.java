package rs.kunpero.vacation.util;

import com.github.seratch.jslack.api.model.block.ActionsBlock;
import com.github.seratch.jslack.api.model.block.DividerBlock;
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

import static rs.kunpero.vacation.util.ActionId.ADD_VACATION;
import static rs.kunpero.vacation.util.ActionId.SET_FROM;
import static rs.kunpero.vacation.util.ActionId.SET_SUBSTITUTION;
import static rs.kunpero.vacation.util.ActionId.SET_TO;

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
                                            .build()))
                            .build()))
            .build();

    public static final View ADD_VACATION_INFO_VIEW = View.builder()
            .type("modal")
            .callbackId("callback_id")
            .title(ViewTitle.builder().type("plain_text").text("New vacation info").build())
            .submit(ViewSubmit.builder().type("plain_text").text("Submit").build())
            .notifyOnClose(true)
            .blocks(List.of(
                    SectionBlock.builder()
                            .text(MarkdownTextObject.builder()
                                    .text("*Fill down your new vacation info:*").build()).build(),
                    SectionBlock.builder()
                            .text(MarkdownTextObject.builder()
                                    .text("Vacation start").build())
                            .accessory(DatePickerElement.builder()
                                    .actionId(SET_FROM.name())
                                    .placeholder(PlainTextObject.builder()
                                            .emoji(true)
                                            .text("Select a date").build()).build())
                            .build(),
                    SectionBlock.builder()
                            .text(MarkdownTextObject.builder()
                                    .text("Vacation end").build())
                            .accessory(DatePickerElement.builder()
                                    .actionId(SET_TO.name())
                                    .placeholder(PlainTextObject.builder()
                                            .emoji(true)
                                            .text("Select a date").build()).build())
                            .build(),
                    SectionBlock.builder()
                            .text(MarkdownTextObject.builder()
                                    .text("Substitution").build())
                            .accessory(MultiUsersSelectElement.builder()
                                    .actionId(SET_SUBSTITUTION.name())
                                    .placeholder(PlainTextObject.builder()
                                            .emoji(true)
                                            .text("Select users").build()).build())
                            .build()))
            .build();
}
