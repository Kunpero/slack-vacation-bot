package rs.kunpero.slackbot.vacation.repository;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
public class VacationAdminRepositoryTest {
    @Autowired
    private VacationAdminRepository vacationAdminRepository;

    @Test
    public void userIsVacationAdminTest() {
        Assert.assertTrue(vacationAdminRepository.existsByUserIdAndTeamId("USER_ADMIN", "TEAM0"));
    }

    @Test
    public void userIsNotVacationAdminTest() {
        Assert.assertFalse(vacationAdminRepository.existsByUserIdAndTeamId("USER_NOT_ADMIN", "TEAM0"));
    }
}
