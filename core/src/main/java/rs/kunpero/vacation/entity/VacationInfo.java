package rs.kunpero.vacation.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity(name = "VACATION_INFO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VacationInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "VACATION_INFO_SEQUENCE")
    private long id;

    @Column(name = "USER_ID", length = 21, nullable = false)
    private String userId;

    @Column(name = "DATE_FROM", nullable = false)
    private LocalDate dateFrom;

    @Column(name = "DATE_TO")
    private LocalDate dateTo;

    @Column(name = "SUBSTITUTION_USER_IDS")
    private String substitutionUserIds;

    public VacationInfo(String userId, LocalDate dateFrom, LocalDate dateTo, String substitutionUserIds) {
        this.userId = userId;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.substitutionUserIds = substitutionUserIds;
    }
}
