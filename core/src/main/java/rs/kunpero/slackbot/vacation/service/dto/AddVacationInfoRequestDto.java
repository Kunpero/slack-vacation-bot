package rs.kunpero.slackbot.vacation.service.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.List;

@Data
@Accessors(chain = true)
public class AddVacationInfoRequestDto {
    private String userId;

    private String teamId;

    private LocalDate dateFrom;

    private LocalDate dateTo;

    private List<String> substitutionIdList;

    private String comment;
}
