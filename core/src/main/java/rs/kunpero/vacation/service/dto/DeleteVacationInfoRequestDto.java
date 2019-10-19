package rs.kunpero.vacation.service.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DeleteVacationInfoRequestDto {
    private String userId;

    private String teamId;

    private long vacationInfoId;
}
