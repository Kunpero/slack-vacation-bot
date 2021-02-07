package rs.kunpero.dutycarousel.scheduler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import rs.kunpero.dutycarousel.entity.DutyList;
import rs.kunpero.dutycarousel.entity.DutyUser;
import rs.kunpero.dutycarousel.repository.DutyListRepository;
import rs.kunpero.vacation.entity.VacationInfo;
import rs.kunpero.vacation.repository.VacationInfoRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;

@Component
@Slf4j
@AllArgsConstructor
public class DutyCarouselTask {
    private final DutyListRepository dutyListRepository;
    private final VacationInfoRepository vacationInfoRepository;
    private final Clock clock;

    @Scheduled(cron = "${next.duty.cron}")
    public void nextDuty() {
        log.info("nextDuty task started");

        List<DutyList> lists = dutyListRepository.findAll();
        if (CollectionUtils.isEmpty(lists)) {
            log.info("No active duty lists");
            return;
        }

        for (DutyList list : lists) {
            processDutyList(list);
        }
    }

    void processDutyList(DutyList list) {
        DutyUser newDutyUser = findNewDutyUser(list);
        dutyListRepository.save(list);
        // send notification
    }

    private DutyUser findNewDutyUser(DutyList list) {
        int maxIterationsValue = list.getUsers().size();
        List<VacationInfo> currentVacations = vacationInfoRepository.findByTeamIdAndDateBetween(list.getTeamId(), LocalDate.now(clock));
        for (Iterator<DutyUser> userIterator = circularIterator(list.getUsers(), list.getUsers().size()); userIterator.hasNext(); ) {
            DutyUser currentDutyUser = userIterator.next();
            if (!currentDutyUser.isOnDuty()) {
                continue;
            }

            int i = 0;
            while (i < maxIterationsValue - 1) {
                i++;
                DutyUser newDutyUser = userIterator.next();
                boolean isOnVacation = currentVacations.stream()
                        .anyMatch(info -> info.getUserId().equals(newDutyUser.getUserId()));
                if (!isOnVacation) {
                    currentDutyUser.setOnDuty(false);
                    newDutyUser.setOnDuty(true);
                    newDutyUser.setLastDateOnDuty(LocalDate.now(clock));
                    return newDutyUser;
                }
            }
            log.warn("New user on duty was not chosen!");
            return currentDutyUser;
        }
        throw new RuntimeException("Unexpected error");
    }

    private static <T> Iterator<T> circularIterator(List<T> list, int count) {
        int size = list.size();
        return new Iterator<>() {

            int i = 0;

            @Override
            public boolean hasNext() {
                return i < count;
            }

            @Override
            public T next() {
                return list.get(i++ % size);
            }
        };
    }
}
