package rs.kunpero.vacation.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import rs.kunpero.vacation.entity.VacationAdmin;

@Repository
public interface VacationAdminRepository extends CrudRepository<VacationAdmin, String> {
    boolean existsByUserIdAndTeamId(String userId, String teamId);
}
