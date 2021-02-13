package rs.kunpero.dutycarousel.scheduler;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import rs.kunpero.dutycarousel.entity.DutyList;
import rs.kunpero.dutycarousel.entity.DutyUser;
import rs.kunpero.dutycarousel.repository.DutyListRepository;
import rs.kunpero.vacation.entity.VacationInfo;
import rs.kunpero.vacation.repository.VacationInfoRepository;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;

import static rs.kunpero.vacation.util.ViewHelperUtils.buildDutyNotifyPostRequest;

@Component
@Slf4j
public class DutyCarouselTask {
    public final static String IS_DAY_OFF_SERVICE_URL = "https://isdayoff.ru/%s";

    private final DutyListRepository dutyListRepository;
    private final VacationInfoRepository vacationInfoRepository;
    private final Clock clock;
    private final OkHttpClient okHttpClient;
    private final MethodsClient methodsClient;

    private final String accessToken;
    private final String channelId;


    @Autowired
    public DutyCarouselTask(DutyListRepository dutyListRepository, VacationInfoRepository vacationInfoRepository,
                            Clock clock, OkHttpClient okHttpClient, MethodsClient methodsClient,
                            @Value("${slack.access.token}") String accessToken, @Value("${duty.notified.channel.id}") String channelId) {
        this.dutyListRepository = dutyListRepository;
        this.vacationInfoRepository = vacationInfoRepository;
        this.clock = clock;
        this.okHttpClient = okHttpClient;
        this.methodsClient = methodsClient;
        this.accessToken = accessToken;
        this.channelId = channelId;
    }

    @Scheduled(cron = "${next.duty.cron}")
    public void nextDuty() throws IOException, SlackApiException {
        log.info("nextDuty task started");

        LocalDate currentDay = LocalDate.now();
        String stringValue = currentDay.toString().replaceAll("-", "");
        Request request = new Request.Builder()
                .url(String.format(IS_DAY_OFF_SERVICE_URL, stringValue))
                .build();
        Response response = okHttpClient.newCall(request).execute();
        String code = response.body().string();
        if ("1".equals(code)) {
            log.info("It's a holiday today! Terminate task...");
            return;
        }
        List<DutyList> lists = dutyListRepository.findAll();
        if (CollectionUtils.isEmpty(lists)) {
            log.info("No active duty lists");
            return;
        }

        for (DutyList list : lists) {
            processDutyList(list);
        }
    }

    void processDutyList(DutyList list) throws IOException, SlackApiException {
        DutyUser newDutyUser = findNewDutyUser(list);
        dutyListRepository.save(list);
        notifyChannel(newDutyUser.getUserId());
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

    private void notifyChannel(String userId) throws IOException, SlackApiException {
        ChatPostMessageResponse response = methodsClient
                .chatPostMessage(buildDutyNotifyPostRequest(accessToken, channelId, userId));
        log.debug(response.toString());
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