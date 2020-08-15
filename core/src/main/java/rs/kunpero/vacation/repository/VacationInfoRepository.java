package rs.kunpero.vacation.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.kunpero.vacation.entity.VacationInfo;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VacationInfoRepository extends CrudRepository<VacationInfo, Long> {
    List<VacationInfo> findByUserIdAndTeamId(String userId, String teamId);

    List<VacationInfo> findByTeamId(String teamId);

    @Query("from VacationInfo v where v.teamId = :teamId and :date between v.dateFrom and v.dateTo")
    List<VacationInfo> findByTeamIdAndDateBetween(@Param("teamId") String teamId, @Param("date") LocalDate date);

    List<VacationInfo> findByTeamIdAndDateToGreaterThanEqual(String teamId, LocalDate date);
}
