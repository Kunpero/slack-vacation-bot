package rs.kunpero.slackbot.vacation.scheduler;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.users.profile.UsersProfileSetRequest;
import com.slack.api.methods.response.users.profile.UsersProfileSetResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import rs.kunpero.slackbot.config.TestConfig;
import rs.kunpero.slackbot.vacation.entity.VacationInfo;
import rs.kunpero.slackbot.vacation.repository.VacationInfoRepository;
import rs.kunpero.slackbot.vacation.service.UserStatusService;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestConfig.class, StatusChangerTask.class, UserStatusService.class})
public class StatusChangerTaskTest {
    @MockBean
    private VacationInfoRepository vacationInfoRepository;
    @MockBean
    private MethodsClient methodsClient;
    @InjectMocks
    @Resource
    private UserStatusService userStatusService;
    @Autowired
    private StatusChangerTask statusChangerTask;


    @Test
    public void expiryTest() throws IOException, SlackApiException {
        ArgumentCaptor<UsersProfileSetRequest> requestArgumentCaptor = ArgumentCaptor.forClass(UsersProfileSetRequest.class);
        final LocalDate fromDate = LocalDate.of(2020, Month.JANUARY, 8);
        final LocalDate toDate = LocalDate.of(2020, Month.JANUARY, 9);


        List<VacationInfo> vacationInfos = List.of(new VacationInfo("user0", "team0", fromDate, toDate,
                "", ""));
        when(vacationInfoRepository.findByDateBetweenAndChangedFalse(any())).thenReturn(vacationInfos);

        UsersProfileSetResponse mockedResponse = new UsersProfileSetResponse();
        mockedResponse.setOk(true);
        when(methodsClient.usersProfileSet(requestArgumentCaptor.capture())).thenReturn(mockedResponse);
        statusChangerTask.changeUserStatus();

        UsersProfileSetRequest value = requestArgumentCaptor.getValue();
        Assert.assertEquals((long) value.getProfile().getStatusExpiration(), vacationInfos.get(0).getDateTo().plusDays(1)
                .atStartOfDay(ZoneId.systemDefault()).toEpochSecond());
    }
}
