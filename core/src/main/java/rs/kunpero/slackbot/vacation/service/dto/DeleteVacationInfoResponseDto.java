package rs.kunpero.slackbot.vacation.service.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class DeleteVacationInfoResponseDto {
    List<VacationInfoDto> vacationInfoList;
}
