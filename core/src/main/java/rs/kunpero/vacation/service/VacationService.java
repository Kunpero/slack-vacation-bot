package rs.kunpero.vacation.service;

import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rs.kunpero.vacation.entity.VacationInfo;
import rs.kunpero.vacation.repository.VacationInfoRepository;
import rs.kunpero.vacation.service.dto.AddVacationInfoRequestDto;
import rs.kunpero.vacation.service.dto.AddVacationInfoResponseDto;
import rs.kunpero.vacation.service.dto.DeleteVacationInfoRequestDto;
import rs.kunpero.vacation.service.dto.DeleteVacationInfoResponseDto;
import rs.kunpero.vacation.service.dto.ShowVacationInfoRequestDto;
import rs.kunpero.vacation.service.dto.ShowVacationInfoResponseDto;
import rs.kunpero.vacation.service.dto.VacationInfoDto;
import rs.kunpero.vacation.util.MessageSourceHelper;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static rs.kunpero.vacation.util.VacationUtils.convertListToString;
import static rs.kunpero.vacation.util.VacationUtils.isWithinRange;
import static rs.kunpero.vacation.util.VacationUtils.wrapIntoInlineMarkdown;
import static rs.kunpero.vacation.util.ViewHelperUtils.buildChatPostRequest;

@Service
@Slf4j
public class VacationService {
    public static final int COMMENT_MAX_LENGTH = 512;

    private static final ReentrantLock LOCK = new ReentrantLock();

    private final VacationInfoRepository vacationInfoRepository;
    private final MessageSourceHelper messageSourceHelper;
    private final Slack slack;

    @Autowired
    public VacationService(VacationInfoRepository vacationInfoRepository, MessageSourceHelper messageSourceHelper,
                           Slack slack) {
        this.vacationInfoRepository = vacationInfoRepository;
        this.messageSourceHelper = messageSourceHelper;
        this.slack = slack;
    }

    @Value("${slack.access.token}")
    private String accessToken;
    @Value("${channel.notification.enabled}")
    private boolean channelNotificationEnabled;
    @Value("${notified.channel.id}")
    private String channelId;

    public AddVacationInfoResponseDto addVacationInfo(AddVacationInfoRequestDto request) throws IOException, SlackApiException {
        var dateFrom = request.getDateFrom();
        var dateTo = request.getDateTo();

        List<String> errorMessages = new ArrayList<>();
        validatePeriod(request.getUserId(), request.getTeamId(), dateFrom, dateTo).ifPresent(errorMessages::add);
        validateComment(request.getComment()).ifPresent(errorMessages::add);
        if (!errorMessages.isEmpty()) {
            return buildResponse(errorMessages);
        }

        String substitutionUserIds = convertListToString(request.getSubstitutionIdList());
        VacationInfo vacationInfo = new VacationInfo(request.getUserId(), request.getTeamId(), dateFrom, dateTo,
                substitutionUserIds, request.getComment());
        vacationInfoRepository.save(vacationInfo);

        log.info("VacationInfo was saved successfully");

        notifySelectedChannel(request.getTeamId());
        return buildResponse(Collections.singletonList("add.vacation.success"));
    }

    public ShowVacationInfoResponseDto showVacationInfo(ShowVacationInfoRequestDto request) {
        String teamId = request.getTeamId();
        List<VacationInfo> vacationInfoList =
                request.isAdmin() ? vacationInfoRepository.findByTeamId(teamId)
                        : vacationInfoRepository.findByUserIdAndTeamId(request.getUserId(), teamId);
        vacationInfoList.removeIf(v -> v.getDateTo().isBefore(LocalDate.now()));
        return new ShowVacationInfoResponseDto()
                .setVacationInfoList(buildVacationInfoDtoListForUser(vacationInfoList));
    }

    public DeleteVacationInfoResponseDto deleteVacationInfo(DeleteVacationInfoRequestDto request) throws IOException, SlackApiException {
        vacationInfoRepository.deleteById(request.getVacationInfoId());
        log.info("VacationInfo with id [{}] was successfully deleted", request.getVacationInfoId());
        String teamId = request.getTeamId();
        List<VacationInfo> vacationInfoList =
                request.isAdmin() ? vacationInfoRepository.findByTeamId(teamId)
                        : vacationInfoRepository.findByUserIdAndTeamId(request.getUserId(), teamId);

        notifySelectedChannel(request.getTeamId());
        return new DeleteVacationInfoResponseDto()
                .setVacationInfoList(buildVacationInfoDtoListForUser(vacationInfoList));
    }

    public ShowVacationInfoResponseDto showCurrentDayVacationInfo(String teamId) {
        LocalDate date = LocalDate.now();
        List<VacationInfo> vacationInfoList = vacationInfoRepository.findByTeamIdAndDateBetween(teamId, date);
        return new ShowVacationInfoResponseDto()
                .setVacationInfoList(buildVacationInfoDtoList(vacationInfoList));
    }

    public ShowVacationInfoResponseDto showAllActualVacations(String teamId) {
        LocalDate date = LocalDate.now();
        List<VacationInfo> vacationInfoList = vacationInfoRepository.findByTeamIdAndDateToGreaterThanEqual(teamId, date);

        return new ShowVacationInfoResponseDto()
                .setVacationInfoList(buildVacationInfoDtoList(vacationInfoList));
    }

    private void notifySelectedChannel(String teamId) throws IOException, SlackApiException {
        if (!channelNotificationEnabled) {
            return;
        }

        try {
            LOCK.lock();
            LocalDate date = LocalDate.now();
            List<VacationInfo> vacationInfoList = vacationInfoRepository.findByTeamIdAndDateToGreaterThanEqual(teamId, date);
            ChatPostMessageResponse response = slack.methods(accessToken)
                    .chatPostMessage(buildChatPostRequest(accessToken, channelId, buildVacationInfoDtoList(vacationInfoList)));
            log.debug(response.toString());
        } finally {
            LOCK.unlock();
        }
    }

    private Optional<String> validatePeriod(String userId, String teamId, LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            log.warn("dateFrom [{}] must be before dateTo [{}]", from, to);
            return Optional.of("vacation.period.wrong.sequence");
        }
        var isInterfered = checkVacationsForUser(userId, teamId,
                from, to);
        if (isInterfered) {
            log.warn("Already has vacation at this period: [{} - {}]", from, to);
            return Optional.of("vacation.period.interfere.error");
        }
        return Optional.empty();
    }

    private Optional<String> validateComment(String comment) {
        final int commentMaxLength = COMMENT_MAX_LENGTH;
        if (comment != null) {
            final int commentLength = comment.length();
            if (commentLength > commentMaxLength) {
                log.warn("Comment length was [{}], but max length is [{}]", commentLength, commentMaxLength);
                return Optional.of("vacation.comment.length.exceeded");
            }
        }
        return Optional.empty();
    }

    private boolean checkVacationsForUser(String userId, String teamId, LocalDate from, LocalDate to) {
        var userVacations = vacationInfoRepository.findByUserIdAndTeamId(userId, teamId);
        return userVacations.stream()
                .anyMatch(v -> isWithinRange(v.getDateFrom(), from, to) ||
                        isWithinRange(v.getDateTo(), from, to));
    }

    private List<VacationInfoDto> buildVacationInfoDtoList(List<VacationInfo> vacationInfoList) {
        return vacationInfoList.stream()
                .sorted(Comparator.comparing(VacationInfo::getDateFrom))
                .map(v -> new VacationInfoDto()
                        .setVacationInfo(String.format("<@%s> `%s` - `%s` %s %s", v.getUserId(), v.getDateFrom(), v.getDateTo(),
                                formSubstitutionUserList(v.getSubstitutionUserIds()), v.getComment() != null ? "\n" + v.getComment() : ""))
                        .setVacationId(v.getId()))
                .collect(toList());
    }

    private List<VacationInfoDto> buildVacationInfoDtoListForUser(List<VacationInfo> vacationInfoList) {
        return vacationInfoList.stream()
                .sorted(Comparator.comparing(VacationInfo::getDateFrom))
                .map(v -> new VacationInfoDto()
                        .setVacationInfo(String.format("`%s` - `%s` %s %s", v.getDateFrom(), v.getDateTo(),
                                formSubstitutionUserList(v.getSubstitutionUserIds()), v.getComment() != null ? "\n" + v.getComment() : ""))
                        .setVacationId(v.getId()))
                .collect(toList());
    }

    private String formSubstitutionUserList(String substitutionUserIds) {
        if (substitutionUserIds == null) {
            return "";
        }
        return Arrays.stream(substitutionUserIds.split(","))
                .filter(u -> !u.isBlank())
                .map(u -> String.format("<@%s>", u))
                .collect(joining(", "));
    }

    private AddVacationInfoResponseDto buildResponse(List<String> sourceList) {
        return new AddVacationInfoResponseDto()
                .setErrorCode(messageSourceHelper.getCode(sourceList.get(0)))
                .setErrorDescription(sourceList.stream()
                        .map(s -> wrapIntoInlineMarkdown(messageSourceHelper.getMessage(s, null)))
                        .collect(joining("\n")));
    }
}
