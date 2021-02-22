package rs.kunpero.slackbot.dutycarousel.service;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rs.kunpero.slackbot.dutycarousel.entity.DutyList;
import rs.kunpero.slackbot.dutycarousel.repository.DutyListRepository;
import rs.kunpero.slackbot.dutycarousel.service.dto.SetPersonOnDutyResponse;
import rs.kunpero.slackbot.vacation.util.MessageSourceHelper;

import java.io.IOException;

import static rs.kunpero.slackbot.vacation.util.ViewHelperUtils.buildDutyNotifyPostRequest;

@Service
@Slf4j
@RequiredArgsConstructor
public class DutyService {
    private final DutyListRepository dutyListRepository;
    private final MessageSourceHelper messageSourceHelper;
    private final MethodsClient methodsClient;

    public DutyList findDutyList(String channelId) {
        return dutyListRepository.findByChannelId(channelId);
    }

    public SetPersonOnDutyResponse setPersonOnDuty(String channelId, String userId) throws IOException, SlackApiException {
        if (!dutyListRepository.existsByChannelIdAndUserId(channelId, userId)) {
            return buildSetDutyResponse("duty.user.not.found");
        }
        DutyList dutyList = dutyListRepository.findByChannelId(channelId);

        dutyList.getUsers()
                .forEach(u -> {
                    if (u.getUserId().equals(userId)) {
                        u.setOnDuty(true);
                    } else {
                        u.setOnDuty(false);
                    }
                });
        dutyListRepository.save(dutyList);
        notifyChannel(userId, channelId);
        return buildSetDutyResponse("set.duty.user.success.message");
    }

    private void notifyChannel(String userId, String channelId) throws IOException, SlackApiException {
        ChatPostMessageResponse response = methodsClient
                .chatPostMessage(buildDutyNotifyPostRequest(channelId, userId));
        log.debug(response.toString());
    }

    private SetPersonOnDutyResponse buildSetDutyResponse(String source) {
        return new SetPersonOnDutyResponse()
                .setErrorCode(messageSourceHelper.getCode(source))
                .setErrorDescription(messageSourceHelper.getMessage(source, null));
    }
}
