package rs.kunpero.vacation.service.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ShowVacationInfoResponseDto {
    List<ShowVacationInfo> vacationInfoList;

    @Data
    @Accessors(chain = true)
    public static class ShowVacationInfo {
        private String vacationInfo;

        private long vacationId;
    }
}
