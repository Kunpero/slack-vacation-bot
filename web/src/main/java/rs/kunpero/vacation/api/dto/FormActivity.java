package rs.kunpero.vacation.api.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class FormActivity {
    private LocalDate dateFrom;

    private LocalDate dateTo;

    private List<String> substitution;
}
