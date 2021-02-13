package rs.kunpero.slackbot.vacation.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import rs.kunpero.slackbot.vacation.entity.VacationInfo;
import rs.kunpero.slackbot.vacation.repository.VacationInfoRepository;
import rs.kunpero.slackbot.vacation.service.UserStatusService;

import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
public class StatusChangerTask {
    private final VacationInfoRepository vacationInfoRepository;
    private final UserStatusService userStatusService;

    @Autowired
    public StatusChangerTask(VacationInfoRepository vacationInfoRepository, UserStatusService userStatusService) {
        this.vacationInfoRepository = vacationInfoRepository;
        this.userStatusService = userStatusService;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void changeUserStatus() {
        log.info("changeUserStatus task started");

        List<VacationInfo> actualVacations = vacationInfoRepository.findByDateBetweenAndChangedFalse(LocalDate.now());
        for (VacationInfo info : actualVacations) {
            userStatusService.changeUserStatus(info);
        }
    }
}