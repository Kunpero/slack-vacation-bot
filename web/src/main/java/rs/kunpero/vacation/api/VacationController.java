package rs.kunpero.vacation.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import rs.kunpero.vacation.api.dto.AddVacationInfoRequest;
import rs.kunpero.vacation.api.dto.AddVacationInfoResponse;
import rs.kunpero.vacation.service.VacationService;
import rs.kunpero.vacation.service.dto.AddVacationInfoRequestDto;
import rs.kunpero.vacation.service.dto.AddVacationInfoResponseDto;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static rs.kunpero.vacation.util.converter.VacationConverterUtils.convert;

@RestController("/vacation")
@Slf4j
public class VacationController implements VacationControllerMeta {

    @Autowired
    private VacationService vacationService;

    @RequestMapping(name = "/addVacation.do", method = RequestMethod.POST, produces = APPLICATION_JSON_UTF8_VALUE)
    public AddVacationInfoResponse addVacation(@Valid @RequestBody AddVacationInfoRequest request) {
        log.info("Incoming addVacation request: [{}]", request);

        AddVacationInfoRequestDto requestDto = convert(request);

        AddVacationInfoResponseDto responseDto = vacationService.addVacationInfo(requestDto);
        AddVacationInfoResponse response = new AddVacationInfoResponse()
                .setErrorCode(responseDto.getErrorCode())
                .setErrorDescription(responseDto.getErrorDescription());
        log.info("Outcome addVacation response: [{}]", response);
        return response;
    }
}
