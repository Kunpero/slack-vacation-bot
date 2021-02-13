package rs.kunpero.slackbot.dutycarousel.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "DUTY_LIST")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DutyList {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "duty_list_generator")
    @SequenceGenerator(name = "duty_list_generator", sequenceName = "SEQ_DUTY_LIST", allocationSize = 1)
    private Long id;

    @Column(name = "TEAM_ID", length = 21, nullable = false)
    private String teamId;

    @Column(name = "MNEMONIC_NAME", length = 100)
    private String mnemonicName;

    @OneToMany(mappedBy = "dutyList", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderBy("positionId ASC")
    private List<DutyUser> users;
}
