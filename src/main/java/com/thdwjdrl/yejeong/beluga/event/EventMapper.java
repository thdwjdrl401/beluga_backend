package com.thdwjdrl.yejeong.beluga.event;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface EventMapper {

	@Insert("""
			INSERT INTO events (
			    event_name,
			    product_name,
			    representative_attach_id,
			    start_at,
			    end_at,
			    winner_limit,
			    winner_count,
			    participant_count,
			    status,
			    created_by,
			    created_at
			) VALUES (
			    #{eventName},
			    #{productName},
			    #{representativeAttachId},
			    #{startAt},
			    #{endAt},
			    #{winnerLimit},
			    #{winnerCount},
			    #{participantCount},
			    #{status},
			    #{createdBy},
			    #{createdAt}
			)
			""")
	
	@Options(useGeneratedKeys = true, keyProperty = "eventId")
	void insert(Event event);

	@Select("""
			SELECT
			    event_id,
			    event_name,
			    product_name,
			    representative_attach_id,
			    start_at,
			    end_at,
			    winner_limit,
			    winner_count,
			    participant_count,
			    status,
			    created_by,
			    created_at,
			    updated_at
			FROM events
			WHERE event_id = #{eventId}
			""")
	Event findById(@Param("eventId") Long eventId);

	@Select("""
			SELECT
			    event_id,
			    event_name,
			    product_name,
			    representative_attach_id,
			    start_at,
			    end_at,
			    winner_limit,
			    winner_count,
			    participant_count,
			    status,
			    created_by,
			    created_at,
			    updated_at
			FROM events
			WHERE event_id = #{eventId}
			FOR UPDATE
			""")
	Event findByIdForUpdate(@Param("eventId") Long eventId);

	@Select("""
			SELECT
			    event_id,
			    event_name,
			    product_name,
			    representative_attach_id,
			    start_at,
			    end_at,
			    winner_limit,
			    winner_count,
			    participant_count,
			    status,
			    created_by,
			    created_at,
			    updated_at
			FROM events
			ORDER BY start_at ASC, event_id ASC
			""")
	List<Event> findAll();

	@Update("""
			UPDATE events
			SET winner_count = #{winnerCount},
			    participant_count = #{participantCount},
			    updated_at = #{updatedAt}
			WHERE event_id = #{eventId}
			""")
	int updateProgress(
			@Param("eventId") Long eventId,
			@Param("winnerCount") int winnerCount,
			@Param("participantCount") int participantCount,
			@Param("updatedAt") java.time.LocalDateTime updatedAt
	);

}
