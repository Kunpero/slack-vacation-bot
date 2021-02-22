package rs.kunpero.slackbot.dutycarousel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.kunpero.slackbot.dutycarousel.entity.DutyList;

@Repository
public interface DutyListRepository extends JpaRepository<DutyList, Long> {
    DutyList findByChannelId(String channelId);

    @Query("select case when (count(l) > 0) then true else false end from DutyList l join DutyUser u on l.id=u.dutyList where l.channelId = :channelId and u.userId = :userId")
    boolean existsByChannelIdAndUserId(@Param("channelId") String channelId, @Param("userId") String userId);
}