package rs.kunpero.slackbot.vacation.service;

import com.slack.api.methods.SlackApiException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import rs.kunpero.slackbot.config.TestConfig;
import rs.kunpero.slackbot.vacation.entity.VacationInfo;
import rs.kunpero.slackbot.vacation.repository.VacationInfoRepository;
import rs.kunpero.slackbot.vacation.service.dto.AddVacationInfoRequestDto;
import rs.kunpero.slackbot.vacation.util.MessageSourceHelper;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static rs.kunpero.slackbot.config.TestConfig.NOW;
import static rs.kunpero.slackbot.vacation.service.VacationService.COMMENT_MAX_LENGTH;
import static rs.kunpero.slackbot.vacation.util.VacationUtils.wrapIntoInlineMarkdown;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestConfig.class, VacationService.class, MessageSourceHelper.class})
public class VacationServiceTest {

    @MockBean
    private VacationInfoRepository vacationInfoRepository;
    @MockBean
    private UserStatusService userStatusService;
    @Autowired
    private VacationService vacationService;

    @Test
    public void successfulAddOperationTest() throws IOException, SlackApiException {
        var userId = "USER0";
        var teamId = "TEAM0";
        var from = LocalDate.of(2018, Month.JUNE, 17);
        var to = from.plusDays(1);
        var substitutionUserIds = List.of("USER1", "USER2");

        var request = new AddVacationInfoRequestDto()
                .setUserId(userId)
                .setTeamId(teamId)
                .setDateFrom(from)
                .setDateTo(to)
                .setSubstitutionIdList(substitutionUserIds);
        when(vacationInfoRepository.findByUserIdAndTeamId(anyString(), anyString())).thenReturn(Collections.emptyList());
        ArgumentCaptor<VacationInfo> vacationInfoArgumentCaptor1 = ArgumentCaptor.forClass(VacationInfo.class);

        var response = vacationService.addVacationInfo(request);
        verify(vacationInfoRepository, times(1)).save(vacationInfoArgumentCaptor1.capture());
        verify(userStatusService, times(0)).changeUserStatus(any());
        Assert.assertEquals("USER1,USER2", vacationInfoArgumentCaptor1.getValue().getSubstitutionUserIds());
        Assert.assertEquals(0, response.getErrorCode());
        Assert.assertEquals(wrapIntoInlineMarkdown("add.vacation.success.message"), response.getErrorDescription());
    }

    @Test
    public void successfulAddOperationTestWithChangeStatusDateFromEqualsNow() throws IOException, SlackApiException {
        var userId = "USER0";
        var teamId = "TEAM0";
        var from = NOW;
        var to = from.plusDays(1);

        var request = new AddVacationInfoRequestDto()
                .setUserId(userId)
                .setTeamId(teamId)
                .setDateFrom(from)
                .setDateTo(to);
        when(vacationInfoRepository.findByUserIdAndTeamId(anyString(), anyString())).thenReturn(Collections.emptyList());

        vacationService.addVacationInfo(request);
        verify(vacationInfoRepository, times(1)).save(any());
        verify(userStatusService, times(1)).changeUserStatus(any());
    }

    @Test
    public void successfulAddOperationTestWithChangeStatusDayFromBeforeNow() throws IOException, SlackApiException {
        var userId = "USER1";
        var teamId = "TEAM1";
        var from = NOW.minusDays(1);
        var to = from.plusDays(1);

        var request = new AddVacationInfoRequestDto()
                .setUserId(userId)
                .setTeamId(teamId)
                .setDateFrom(from)
                .setDateTo(to);
        when(vacationInfoRepository.findByUserIdAndTeamId(anyString(), anyString())).thenReturn(Collections.emptyList());

        vacationService.addVacationInfo(request);
        verify(vacationInfoRepository, times(1)).save(any());
        verify(userStatusService, times(1)).changeUserStatus(any());
    }

    @Test
    public void successfulAddOperationTestWithChangeStatusDayToEqualsNow() throws IOException, SlackApiException {
        var userId = "USER1";
        var teamId = "TEAM1";
        var from = NOW.minusDays(1);
        var to = NOW;

        var request = new AddVacationInfoRequestDto()
                .setUserId(userId)
                .setTeamId(teamId)
                .setDateFrom(from)
                .setDateTo(to);
        when(vacationInfoRepository.findByUserIdAndTeamId(anyString(), anyString())).thenReturn(Collections.emptyList());

        vacationService.addVacationInfo(request);
        verify(vacationInfoRepository, times(1)).save(any());
        verify(userStatusService, times(1)).changeUserStatus(any());
    }

    @Test
    public void successfulAddOperationTestWithChangeStatusOneDayVacation() throws IOException, SlackApiException {
        var userId = "USER2";
        var teamId = "TEAM2";
        var from = NOW;
        var to = from;

        var request = new AddVacationInfoRequestDto()
                .setUserId(userId)
                .setTeamId(teamId)
                .setDateFrom(from)
                .setDateTo(to);
        when(vacationInfoRepository.findByUserIdAndTeamId(anyString(), anyString())).thenReturn(Collections.emptyList());

        vacationService.addVacationInfo(request);
        verify(vacationInfoRepository, times(1)).save(any());
        verify(userStatusService, times(1)).changeUserStatus(any());
    }

    @Test
    public void vacationPeriodDateFromIsAfterDateToErrorTest() throws IOException, SlackApiException {
        var userId = "USER0";
        var teamId = "TEAM0";
        var from = LocalDate.of(2018, Month.JUNE, 17);
        var to = from.minusDays(2);
        var request = new AddVacationInfoRequestDto()
                .setUserId(userId)
                .setTeamId(teamId)
                .setDateFrom(from)
                .setDateTo(to)
                .setSubstitutionIdList(null);
        var response = vacationService.addVacationInfo(request);
        Assert.assertEquals(3, response.getErrorCode());
        Assert.assertEquals(wrapIntoInlineMarkdown("vacation.period.wrong.sequence.message"), response.getErrorDescription());

    }

    @Test
    public void vacationPeriodInterfereErrorTest() throws IOException, SlackApiException {
        var userId = "USER0";
        var teamId = "TEAM0";
        var from = LocalDate.of(2018, Month.JUNE, 17);
        var to = from.plusDays(2);
        var substitutionUserIds = List.of("USER1", "USER2");
        final String comment = "Comment";

        var interferedVacation = new VacationInfo(0L, teamId, userId, from.plusDays(1), to.plusDays(1), "USER1,USER2",
                comment, false);
        when(vacationInfoRepository.findByUserIdAndTeamId(anyString(), anyString())).thenReturn(List.of(interferedVacation));

        var request = new AddVacationInfoRequestDto()
                .setUserId(userId)
                .setTeamId(teamId)
                .setDateFrom(from)
                .setDateTo(to)
                .setSubstitutionIdList(substitutionUserIds);
        var response = vacationService.addVacationInfo(request);
        Assert.assertEquals(3, response.getErrorCode());
        Assert.assertEquals(wrapIntoInlineMarkdown("vacation.period.interfere.error.message"), response.getErrorDescription());
    }

    @Test
    public void vacationWrongPeriodAndCommentErrorTest() throws IOException, SlackApiException {
        var userId = "USER0";
        var teamId = "TEAM0";
        var from = LocalDate.of(2018, Month.JUNE, 17);
        var to = from.plusDays(2);
        var substitutionUserIds = List.of("USER1", "USER2");
        final String comment = "A".repeat(COMMENT_MAX_LENGTH + 1);

        var interferedVacation = new VacationInfo(0L, teamId, userId, from.plusDays(1), to.plusDays(1), "USER1,USER2",
                comment, false);
        when(vacationInfoRepository.findByUserIdAndTeamId(anyString(), anyString())).thenReturn(List.of(interferedVacation));

        var request = new AddVacationInfoRequestDto()
                .setUserId(userId)
                .setTeamId(teamId)
                .setDateFrom(from)
                .setDateTo(to)
                .setSubstitutionIdList(substitutionUserIds)
                .setComment(comment);
        var response = vacationService.addVacationInfo(request);
        Assert.assertEquals(3, response.getErrorCode());
        Assert.assertEquals(wrapIntoInlineMarkdown("vacation.period.interfere.error.message") + "\n" + wrapIntoInlineMarkdown("vacation.comment.length.exceeded.message"), response.getErrorDescription());
    }
}
