package rs.kunpero.vacation.service.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class VacationInfoDto {
    private String vacationInfo;

    private long vacationId;
}
