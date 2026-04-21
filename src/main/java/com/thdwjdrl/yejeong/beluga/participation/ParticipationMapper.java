package com.thdwjdrl.yejeong.beluga.participation;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ParticipationMapper {

	@Insert("""
			INSERT INTO event_participations (
			    event_id,
			    user_id,
			    participated_at,
			    result_status,
			    request_sequence,
			    created_at
			) VALUES (
			    #{eventId},
			    #{userId},
			    #{participatedAt},
			    #{resultStatus},
			    #{requestSequence},
			    #{createdAt}
			)
			""")
	@Options(useGeneratedKeys = true, keyProperty = "participationId")
	void insert(Participation participation);

	@Select("""
			SELECT
			    participation_id,
			    event_id,
			    user_id,
			    participated_at,
			    result_status,
			    request_sequence,
			    created_at
			FROM event_participations
			WHERE event_id = #{eventId}
			  AND user_id = #{userId}
			""")
	Participation findByEventIdAndUserId(@Param("eventId") Long eventId, @Param("userId") Long userId);

	@Select("""
			SELECT
			    ep.participation_id,
			    ep.event_id,
			    ep.user_id,
			    u.email AS user_email,
			    ep.participated_at,
			    ep.result_status,
			    ep.request_sequence,
			    ep.created_at
			FROM event_participations ep
			JOIN users u ON u.user_id = ep.user_id
			WHERE ep.event_id = #{eventId}
			ORDER BY ep.request_sequence ASC
			""")
	List<ParticipationHistoryRow> findParticipantsByEventId(@Param("eventId") Long eventId);

	@Select("""
			SELECT
			    ep.participation_id,
			    ep.event_id,
			    ep.user_id,
			    u.email AS user_email,
			    ep.participated_at,
			    ep.result_status,
			    ep.request_sequence,
			    ep.created_at
			FROM event_participations ep
			JOIN users u ON u.user_id = ep.user_id
			WHERE ep.event_id = #{eventId}
			  AND ep.result_status = 'WIN'
			ORDER BY ep.request_sequence ASC
			""")
	List<ParticipationHistoryRow> findWinnersByEventId(@Param("eventId") Long eventId);

}
