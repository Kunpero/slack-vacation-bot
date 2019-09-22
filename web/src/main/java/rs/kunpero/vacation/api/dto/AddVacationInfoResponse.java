package rs.kunpero.vacation.api.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AddVacationInfoResponse {
    private int errorCode;

    private String errorDescription;
}
