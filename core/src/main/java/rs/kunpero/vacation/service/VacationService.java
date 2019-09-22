package rs.kunpero.vacation.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.kunpero.vacation.entity.VacationInfo;
import rs.kunpero.vacation.repository.VacationInfoRepository;
import rs.kunpero.vacation.service.dto.AddVacationInfoRequestDto;
import rs.kunpero.vacation.service.dto.AddVacationInfoResponseDto;
import rs.kunpero.vacation.util.MessageSourceHelper;
import rs.kunpero.vacation.util.VacationUtils;

import java.time.LocalDate;

import static rs.kunpero.vacation.util.VacationUtils.convertListToString;
import static rs.kunpero.vacation.util.VacationUtils.isWithinRange;

@Service
@Slf4j
public class VacationService {

    @Autowired
    private VacationInfoRepository vacationInfoRepository;
    @Autowired
    private MessageSourceHelper messageSourceHelper;

    public AddVacationInfoResponseDto addVacationInfo(AddVacationInfoRequestDto request) {

        boolean isInterfered = validatePeriod(request.getUserId(), request.getDateFrom(), request.getDateTo());
        if (isInterfered) {
            log.warn("Already has vacation at this period: [{} - {}]", request.getDateFrom(), request.getDateTo());
            return buildResponse("vacation.period.interfere.error");
        }
        String substitutionUserIds = convertListToString(request.getSubstitutionIdList());
        VacationInfo vacationInfo = new VacationInfo(request.getUserId(), request.getDateFrom(), request.getDateTo(), substitutionUserIds);
        vacationInfoRepository.save(vacationInfo);

        log.info("VacationInfo was saved successfully");
        return buildResponse("add.vacation.success");
    }

    private boolean validatePeriod(String userId, LocalDate from, LocalDate to) {
        var userVacations = vacationInfoRepository.findByUserId(userId);
        return userVacations.stream()
                .anyMatch(v -> isWithinRange(v.getDateFrom(), from, to) ||
                        isWithinRange(v.getDateTo(), from, to));
    }

    private AddVacationInfoResponseDto buildResponse(String source) {
        return new AddVacationInfoResponseDto()
                .setErrorCode(messageSourceHelper.getCode(source))
                .setErrorDescription(messageSourceHelper.getMessage(source, null));
    }
}