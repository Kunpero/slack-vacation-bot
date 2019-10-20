package rs.kunpero.vacation.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.kunpero.vacation.entity.VacationInfo;
import rs.kunpero.vacation.repository.VacationInfoRepository;
import rs.kunpero.vacation.service.dto.AddVacationInfoRequestDto;
import rs.kunpero.vacation.service.dto.AddVacationInfoResponseDto;
import rs.kunpero.vacation.service.dto.DeleteVacationInfoRequestDto;
import rs.kunpero.vacation.service.dto.DeleteVacationInfoResponseDto;
import rs.kunpero.vacation.service.dto.ShowVacationInfoRequestDto;
import rs.kunpero.vacation.service.dto.ShowVacationInfoResponseDto;
import rs.kunpero.vacation.service.dto.VacationInfoDto;
import rs.kunpero.vacation.util.MessageSourceHelper;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static rs.kunpero.vacation.util.VacationUtils.convertListToString;
import static rs.kunpero.vacation.util.VacationUtils.isWithinRange;

@Service
@Slf4j
public class VacationService {
    private final VacationInfoRepository vacationInfoRepository;
    private final MessageSourceHelper messageSourceHelper;

    @Autowired
    public VacationService(VacationInfoRepository vacationInfoRepository, MessageSourceHelper messageSourceHelper) {
        this.vacationInfoRepository = vacationInfoRepository;
        this.messageSourceHelper = messageSourceHelper;
    }

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

    public ShowVacationInfoResponseDto showVacationInfo(ShowVacationInfoRequestDto request) {
        List<VacationInfo> vacationInfoList = vacationInfoRepository.findByUserIdAndTeamId(request.getUserId(), request.getTeamId());
        return new ShowVacationInfoResponseDto()
                .setVacationInfoList(buildVacationInfoDtoListForUser(vacationInfoList));
    }

    public DeleteVacationInfoResponseDto deleteVacationInfo(DeleteVacationInfoRequestDto request) {
        vacationInfoRepository.deleteById(request.getVacationInfoId());
        log.info("VacationInfo with id [{}] was successfully deleted", request.getVacationInfoId());
        List<VacationInfo> vacationInfoList = vacationInfoRepository.findByUserIdAndTeamId(request.getUserId(), request.getTeamId());
        return new DeleteVacationInfoResponseDto()
                .setVacationInfoList(buildVacationInfoDtoListForUser(vacationInfoList));
    }

    public ShowVacationInfoResponseDto showCurrentDayVacationInfo(String teamId) {
        LocalDate date = LocalDate.now();
        List<VacationInfo> vacationInfoList = vacationInfoRepository.findByTeamIdAndDateBetween(teamId, date);
        return new ShowVacationInfoResponseDto()
                .setVacationInfoList(buildVacationInfoDtoList(vacationInfoList));
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

    private List<VacationInfoDto> buildVacationInfoDtoList(List<VacationInfo> vacationInfoList) {
        return vacationInfoList.stream()
                .sorted(Comparator.comparing(VacationInfo::getDateFrom))
                .map(v -> new VacationInfoDto()
                        .setVacationInfo(String.format("<@%s> `%s` - `%s` %s", v.getUserId(), v.getDateFrom(), v.getDateTo(),
                                formSubstitutionUserList(v.getSubstitutionUserIds())))
                        .setVacationId(v.getId()))
                .collect(toList());
    }

    private List<VacationInfoDto> buildVacationInfoDtoListForUser(List<VacationInfo> vacationInfoList) {
        return vacationInfoList.stream()
                .sorted(Comparator.comparing(VacationInfo::getDateFrom))
                .map(v -> new VacationInfoDto()
                        .setVacationInfo(String.format("`%s` - `%s` %s", v.getDateFrom(), v.getDateTo(),
                                formSubstitutionUserList(v.getSubstitutionUserIds())))
                        .setVacationId(v.getId()))
                .collect(toList());
    }

    private String formSubstitutionUserList(String substitutionUserIds) {
        if (substitutionUserIds == null) {
            return "";
        }
        return Arrays.stream(substitutionUserIds.split(","))
                .map(u -> String.format("<@%s>", u))
                .collect(joining(", "));
    }

    private AddVacationInfoResponseDto buildResponse(String source) {
        return new AddVacationInfoResponseDto()
                .setErrorCode(messageSourceHelper.getCode(source))
                .setErrorDescription(messageSourceHelper.getMessage(source, null));
    }
}
