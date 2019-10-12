package rs.kunpero.vacation.service.dto;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ToString
public class AddVacationInfoResponseDto {
    private int errorCode;
    private String errorDescription;

    public boolean isSuccesful() {
        return errorCode == 0;
    }
}
