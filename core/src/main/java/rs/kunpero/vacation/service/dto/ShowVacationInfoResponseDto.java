package rs.kunpero.vacation.service.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ShowVacationInfoResponseDto {
    List<VacationInfoDto> vacationInfoList;
}
