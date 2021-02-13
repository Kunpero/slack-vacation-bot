package rs.kunpero.slackbot.dutycarousel.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "DUTY_USER")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DutyUser {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "duty_user_generator")
    @SequenceGenerator(name = "duty_user_generator", sequenceName = "SEQ_DUTY_USER", allocationSize = 1)
    private Long id;

    @Column(name = "POSITION_ID", nullable = false)
    private int positionId;

    @Column(name = "USER_ID", length = 21, nullable = false)
    private String userId;

    @Column(name = "ON_DUTY", nullable = false)
    private boolean onDuty;

    @Column(name = "LAST_DATE_ON_DUTY", nullable = false)
    private LocalDate lastDateOnDuty;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "DUTY_LIST_ID", nullable = false)
    private DutyList dutyList;
}
