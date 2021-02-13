package rs.kunpero.slackbot.vacation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.kunpero.slackbot.vacation.repository.VacationAdminRepository;

@Service
public class VacationAdminService {
    private final VacationAdminRepository vacationAdminRepository;

    @Autowired
    public VacationAdminService(VacationAdminRepository vacationAdminRepository) {
        this.vacationAdminRepository = vacationAdminRepository;
    }

    public boolean isAdmin(String userId, String teamId) {
        return vacationAdminRepository.existsByUserIdAndTeamId(userId, teamId);
    }
}
