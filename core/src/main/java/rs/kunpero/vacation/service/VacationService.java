package rs.kunpero.vacation.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.kunpero.vacation.entity.VacationInfo;
import rs.kunpero.vacation.repository.VacationInfoRepository;
import rs.kunpero.vacation.service.dto.AddVacationInfoRequestDto;
import rs.kunpero.vacation.service.dto.AddVacationInfoResponseDto;
import rs.kunpero.vacation.util.MessageSourceHelper;

import java.time.LocalDate;
import java.util.Optional;

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
        var dateFrom = request.getDateFrom();
        var dateTo = request.getDateTo();

        Optional<String> errorMessage = validatePeriod(request.getUserId(), request.getTeamId(), dateFrom, dateTo);
        if (errorMessage.isPresent()) {
            return buildResponse(errorMessage.get());
        }

        String substitutionUserIds = convertListToString(request.getSubstitutionIdList());
        VacationInfo vacationInfo = new VacationInfo(request.getUserId(), request.getTeamId(),
                dateFrom, dateTo, substitutionUserIds);
        vacationInfoRepository.save(vacationInfo);

        log.info("VacationInfo was saved successfully");
        return buildResponse("add.vacation.success");
    }

    private Optional<String> validatePeriod(String userId, String teamId, LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            log.warn("dateFrom [{}] must be before dateTo [{}]", from, to);
            return Optional.of("vacation.period.wrong.sequence");
        }
        var isInterfered = checkVacationsForUser(userId, teamId,
                from, to);
        if (isInterfered) {
            log.warn("Already has vacation at this period: [{} - {}]", from, to);
            return Optional.of("vacation.period.interfere.error");
        }
        return Optional.empty();
    }

    private boolean checkVacationsForUser(String userId, String teamId, LocalDate from, LocalDate to) {
        var userVacations = vacationInfoRepository.findByUserIdAndTeamId(userId, teamId);
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
