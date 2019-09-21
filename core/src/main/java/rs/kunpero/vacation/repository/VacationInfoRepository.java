package rs.kunpero.vacation.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import rs.kunpero.vacation.entity.VacationInfo;

import java.util.List;

@Repository
public interface VacationInfoRepository extends CrudRepository<VacationInfo, Long> {
    List<VacationInfo> findByUserId(String userId);
}
