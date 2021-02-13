package rs.kunpero.slackbot.vacation.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "VACATION_INFO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VacationInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vacation_info_generator")
    @SequenceGenerator(name = "vacation_info_generator", sequenceName = "SEQ_VACATION_INFO", allocationSize = 1)
    private Long id;

    @Column(name = "USER_ID", length = 21, nullable = false)
    private String userId;

    @Column(name = "TEAM_ID", length = 21, nullable = false)
    private String teamId;

    @Column(name = "DATE_FROM", nullable = false)
    private LocalDate dateFrom;

    @Column(name = "DATE_TO")
    private LocalDate dateTo;

    @Column(name = "SUBSTITUTION_USER_IDS")
    private String substitutionUserIds;

    @Column(name = "COMMENT")
    private String comment;

    @Column(name = "IS_STATUS_CHANGED")
    private boolean statusChanged;

    public VacationInfo(String userId, String teamId, LocalDate dateFrom, LocalDate dateTo, String substitutionUserIds,
                        String comment) {
        this.userId = userId;
        this.teamId = teamId;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.substitutionUserIds = substitutionUserIds;
        this.comment = comment;
    }
}
