package rs.kunpero.dutycarousel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.kunpero.dutycarousel.entity.DutyList;

@Repository
public interface DutyListRepository extends JpaRepository<DutyList, Long> {
}
