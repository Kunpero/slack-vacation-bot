package rs.kunpero.slackbot.dutycarousel.service.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SetPersonOnDutyResponse {
    private int errorCode;
    private String errorDescription;

    public boolean isSuccessful() {
        return errorCode == 0;
    }
}
