package rs.kunpero.vacation.scheduler;

import com.slack.api.methods.AsyncMethodsClient;
import com.slack.api.methods.request.users.profile.UsersProfileSetRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import rs.kunpero.vacation.config.TestConfig;
import rs.kunpero.vacation.entity.VacationInfo;
import rs.kunpero.vacation.repository.VacationInfoRepository;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestConfig.class, StatusChangerTask.class})
public class StatusChangerTaskTest {
    @MockBean
    private VacationInfoRepository vacationInfoRepository;
    @MockBean
    private AsyncMethodsClient methodsClient;
    @Autowired
    private StatusChangerTask statusChangerTask;


    @Test
    public void expiryTest() {
        ArgumentCaptor<UsersProfileSetRequest> requestArgumentCaptor = ArgumentCaptor.forClass(UsersProfileSetRequest.class);
        final LocalDate fromDate = LocalDate.of(2020, Month.JANUARY, 8);
        final LocalDate toDate = LocalDate.of(2020, Month.JANUARY, 9);


        List<VacationInfo> vacationInfos = List.of(new VacationInfo("user0", "team0", fromDate, toDate,
                "", ""));
        when(vacationInfoRepository.findByAndDateToGreaterThanEqualAndChangedFalse(any())).thenReturn(vacationInfos);
        when(methodsClient.usersProfileSet(requestArgumentCaptor.capture())).thenReturn(null);
        statusChangerTask.changeUserStatus();

        UsersProfileSetRequest value = requestArgumentCaptor.getValue();
        Assert.assertEquals((long) value.getProfile().getStatusExpiration(), vacationInfos.get(0).getDateTo().plusDays(1).toEpochDay());
    }
}
