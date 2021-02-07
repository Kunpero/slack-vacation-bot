package rs.kunpero.dutycarousel.repository;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import rs.kunpero.dutycarousel.entity.DutyList;

@RunWith(SpringRunner.class)
@DataJpaTest
public class DutyListRepositoryTest {
    @Autowired
    private DutyListRepository dutyListRepository;

    @Test
    public void dutyListFindTest() {
        DutyList list = dutyListRepository.findById(0L).orElse(null);
        Assert.assertNotNull(list);
        Assert.assertEquals(3, list.getUsers().size());
    }
}
