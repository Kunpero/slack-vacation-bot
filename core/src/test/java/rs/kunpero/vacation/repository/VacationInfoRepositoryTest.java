package rs.kunpero.vacation.repository;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import rs.kunpero.vacation.entity.VacationInfo;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

@RunWith(SpringRunner.class)
@DataJpaTest
public class VacationInfoRepositoryTest {
    @Autowired
    private VacationInfoRepository vacationInfoRepository;

    @Test
    public void findByUserIdTestAndTeamId() {
        final String userId = "USER0";
        final String teamId = "TEAM0";
        final String substitution = "USER1,USER2";

        List<VacationInfo> userVacations = vacationInfoRepository.findByUserIdAndTeamId(userId, teamId);
        Assert.assertEquals(1, userVacations.size());


        VacationInfo actualVacation = userVacations.get(0);
        VacationInfo expectedVacation = new VacationInfo(0L,
                userId,
                teamId,
                LocalDate.of(2018, Month.JUNE, 16),
                LocalDate.of(2018, Month.JUNE, 18),
                substitution);
        Assert.assertEquals(expectedVacation, actualVacation);
    }

    @Test
    public void saveTest() {
        final String userId = "USER1";
        final String teamId = "TEAM0";
        final String substitution = "USER0,USER2";
        final LocalDate from = LocalDate.of(2018, Month.JUNE, 19);
        final LocalDate to = LocalDate.of(2018, Month.JUNE, 20);

        VacationInfo savedEntity = vacationInfoRepository.save(new VacationInfo(userId, teamId, from, to, substitution));
        Assert.assertNotNull(savedEntity);
    }

    @Test
    public void findByTeamIdAndDateBetweenTest() {
        final String teamId = "TEAM1";
        final LocalDate date = LocalDate.of(2019, Month.JANUARY, 14);
        List<VacationInfo> userVacations = vacationInfoRepository.findByTeamIdAndDateBetween(teamId, date);
        Assert.assertEquals(2, userVacations.size());
    }
}
