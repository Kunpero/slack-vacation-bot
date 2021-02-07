package rs.kunpero.dutycarousel.scheduler;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import rs.kunpero.config.CarouselTestConfig;
import rs.kunpero.config.TestConfig;
import rs.kunpero.dutycarousel.entity.DutyList;
import rs.kunpero.dutycarousel.entity.DutyUser;
import rs.kunpero.dutycarousel.repository.DutyListRepository;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestConfig.class, CarouselTestConfig.class, DutyCarouselTask.class})
public class DutyCarouselTaskTest {
    @Autowired
    private DutyCarouselTask dutyCarouselTask;
    @Autowired
    private DutyListRepository dutyListRepository;

    @Test
    public void selectNewDutyUserTest() {
        long listId = 0;
        DutyList list = dutyListRepository.findById(listId).orElseThrow();
        dutyCarouselTask.processDutyList(list);

        list = dutyListRepository.findById(listId).orElseThrow();
        List<DutyUser> users = list.getUsers();
        Assert.assertFalse(users.get(0).isOnDuty());
        Assert.assertTrue(users.get(1).isOnDuty());
        Assert.assertFalse(users.get(2).isOnDuty());
    }

    @Test
    public void selectNewDutyUserCircularIterationTest() {
        long listId = 1;
        DutyList list = dutyListRepository.findById(listId).orElseThrow();
        dutyCarouselTask.processDutyList(list);

        list = dutyListRepository.findById(listId).orElseThrow();
        List<DutyUser> users = list.getUsers();
        Assert.assertTrue(users.get(0).isOnDuty());
        Assert.assertFalse(users.get(1).isOnDuty());
        Assert.assertFalse(users.get(2).isOnDuty());
    }

    @Test
    public void selectNewDutyUserSkipWithVacation() {
        long listId = 2;
        DutyList list = dutyListRepository.findById(listId).orElseThrow();
        dutyCarouselTask.processDutyList(list);

        list = dutyListRepository.findById(listId).orElseThrow();
        List<DutyUser> users = list.getUsers();
        Assert.assertTrue(users.get(0).isOnDuty());
        Assert.assertFalse(users.get(1).isOnDuty());
        Assert.assertFalse(users.get(2).isOnDuty());
    }

    @Test
    public void allOtherUsersOnVacationTest() {
        long listId = 3;
        DutyList list = dutyListRepository.findById(listId).orElseThrow();
        dutyCarouselTask.processDutyList(list);

        list = dutyListRepository.findById(listId).orElseThrow();
        List<DutyUser> users = list.getUsers();
        Assert.assertFalse(users.get(0).isOnDuty());
        Assert.assertTrue(users.get(1).isOnDuty());
        Assert.assertFalse(users.get(2).isOnDuty());
    }
}
