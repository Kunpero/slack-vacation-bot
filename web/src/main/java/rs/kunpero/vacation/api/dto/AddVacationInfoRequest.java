package rs.kunpero.vacation.api.dto;

import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Setter
@ToString
@EqualsAndHashCode
public class AddVacationInfoRequest {
    @NotBlank
    private String userId;

    @NotNull
    private LocalDate dateFrom;

    @NotNull
    private LocalDate dateTo;

    private List<String> substitution;

    public String getUserId() {
        return userId;
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public List<String> getSubstitution() {
        return new ArrayList<>(substitution);
    }
}
