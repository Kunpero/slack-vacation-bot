package rs.kunpero.slackbot.dutycarousel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.kunpero.slackbot.dutycarousel.entity.DutyList;

@Repository
public interface DutyListRepository extends JpaRepository<DutyList, Long> {
}
