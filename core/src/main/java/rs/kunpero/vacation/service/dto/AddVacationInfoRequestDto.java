package rs.kunpero.vacation.service.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.List;

@Data
@Accessors(chain = true)
public class AddVacationInfoRequestDto {
    private String userId;

    private LocalDate dateFrom;

    private LocalDate dateTo;

    private List<String> substitutionIdList;

//    public AddVacationInfoRequestDto setSubstitutionIdList(List<String> substitutionIdList) {
//        this.substitutionIdList = Collections.unmodifiableList(substitutionIdList);
//        return this;
//    }
}
