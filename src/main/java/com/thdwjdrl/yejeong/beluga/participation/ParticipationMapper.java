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
			    request_sequence,
			    result_status,
			    gifticon_attach_id,
			    created_at
			) VALUES (
			    #{eventId},
			    #{userId},
			    #{participatedAt},
			    #{requestSequence},
			    #{resultStatus},
			    #{gifticonAttachId},
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
			    request_sequence,
			    result_status,
			    gifticon_attach_id,
			    created_at
			FROM event_participations
			WHERE event_id = #{eventId}
			  AND user_id = #{userId}
			""")
	Participation findByEventIdAndUserId(@Param("eventId") Long eventId, @Param("userId") Long userId);

	@Select("""
			SELECT
			    ep.event_id,
			    e.event_name,
			    e.product_name,
			    ep.participated_at,
			    ep.result_status,
			    ep.gifticon_attach_id,
			    e.start_at,
			    e.end_at
			FROM event_participations ep
			JOIN events e ON e.event_id = ep.event_id
			WHERE ep.user_id = #{userId}
			ORDER BY ep.participated_at DESC, ep.participation_id DESC
			""")
	List<MyParticipationRow> findMyParticipationsByUserId(@Param("userId") Long userId);

	@Select("""
			SELECT
				ep.participation_id,
				ep.event_id,
				u.user_id,
				u.nickname,
				u.email,
				ep.participated_at,
				ep.request_sequence,
				ep.result_status
			FROM event_participations ep
			JOIN users u ON ep.user_id = u.user_id
			WHERE ep.event_id = #{eventId}
			ORDER BY ep.created_at DESC;
			""")
	List<ParticipationsListResponse> findParticipationsByEventId(@Param("eventId") Long eventId);

}
