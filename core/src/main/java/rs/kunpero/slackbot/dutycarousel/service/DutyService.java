package rs.kunpero.slackbot.dutycarousel.service;

import com.slack.api.app_backend.slash_commands.payload.SlashCommandPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rs.kunpero.slackbot.dutycarousel.entity.DutyList;
import rs.kunpero.slackbot.dutycarousel.repository.DutyListRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class DutyService {
    private final DutyListRepository dutyListRepository;

    public DutyList findDutyList(SlashCommandPayload payload) {
        return dutyListRepository.findByChannelId(payload.getChannelId());
    }
}
