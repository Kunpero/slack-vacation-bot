package rs.kunpero.vacation.scheduler;

import com.slack.api.methods.AsyncMethodsClient;
import com.slack.api.methods.request.users.profile.UsersProfileSetRequest;
import com.slack.api.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import rs.kunpero.vacation.entity.VacationInfo;
import rs.kunpero.vacation.repository.VacationInfoRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
@Slf4j
public class StatusChangerTask {

    private final AsyncMethodsClient methodsClient;
    private final VacationInfoRepository vacationInfoRepository;
    private final String accessToken;

    @Autowired
    public StatusChangerTask(AsyncMethodsClient asyncMethodsClient, VacationInfoRepository vacationInfoRepository, @Value("${slack.access.token}") String accessToken) {
        this.methodsClient = asyncMethodsClient;
        this.vacationInfoRepository = vacationInfoRepository;
        this.accessToken = accessToken;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void changeUserStatus() {
        log.info("changeUserStatus task started");

        List<VacationInfo> actualVacations = vacationInfoRepository.findByDateBetweenAndChangedFalse(LocalDate.now());
        actualVacations
                .forEach(info -> {
                    final User.Profile profile = new User.Profile();
                    profile.setStatusEmoji(":palm_tree:");
                    profile.setStatusText(String.format("On vacation until %s", info.getDateTo().toString()));
                    profile.setStatusExpiration(info.getDateTo().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond());
                    UsersProfileSetRequest request = UsersProfileSetRequest.builder()
                            .token(accessToken)
                            .name(info.getUserId())
                            .profile(profile)
                            .build();
                    methodsClient.usersProfileSet(request);
                    info.setChanged(true);
                    log.info("Status with id [{}] for user [{}] was updated", info.getId(), info.getUserId());
                });
        vacationInfoRepository.saveAll(actualVacations);
    }
}