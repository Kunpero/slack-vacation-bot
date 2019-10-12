package rs.kunpero.vacation.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import rs.kunpero.vacation.config.MessageSourceConfig;
import rs.kunpero.vacation.entity.VacationInfo;
import rs.kunpero.vacation.repository.VacationInfoRepository;
import rs.kunpero.vacation.service.dto.AddVacationInfoRequestDto;
import rs.kunpero.vacation.util.MessageSourceHelper;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MessageSourceConfig.class, VacationService.class, MessageSourceHelper.class})
public class VacationServiceTest {

    @MockBean
    private VacationInfoRepository vacationInfoRepository;
    @Autowired
    private VacationService vacationService;

    @Test
    public void successfulAddOperationTest() {
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
        ArgumentCaptor<VacationInfo> vacationInfoArgumentCaptor = ArgumentCaptor.forClass(VacationInfo.class);
        var response = vacationService.addVacationInfo(request);
        verify(vacationInfoRepository, times(1)).save(vacationInfoArgumentCaptor.capture());
        Assert.assertEquals("USER1,USER2", vacationInfoArgumentCaptor.getValue().getSubstitutionUserIds());
        Assert.assertEquals(0, response.getErrorCode());
        Assert.assertEquals("add.vacation.success.message", response.getErrorDescription());
    }

    @Test
    public void vacationPeriodInterfereErrorTest() {
        var userId = "USER0";
        var teamId = "TEAM0";
        var from = LocalDate.of(2018, Month.JUNE, 17);
        var to = from.plusDays(2);
        var substitutionUserIds = List.of("USER1", "USER2");
        var interferedVacation = new VacationInfo(0, teamId, userId, from.plusDays(1), to.plusDays(1), "USER1,USER2");
        when(vacationInfoRepository.findByUserIdAndTeamId(anyString(), anyString())).thenReturn(List.of(interferedVacation));

        var request = new AddVacationInfoRequestDto()
                .setUserId(userId)
                .setTeamId(teamId)
                .setDateFrom(from)
                .setDateTo(to)
                .setSubstitutionIdList(substitutionUserIds);
        var response = vacationService.addVacationInfo(request);
        Assert.assertEquals(3, response.getErrorCode());
        Assert.assertEquals("vacation.period.interfere.error.message", response.getErrorDescription());
    }
}
