package rs.kunpero.slackbot.vacation.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "VACATION_ADMIN")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VacationAdmin {
    @Id
    @Column(name = "USER_ID", length = 21, nullable = false)
    private String userId;

    @Column(name = "TEAM_ID", length = 21, nullable = false)
    private String teamId;
}
