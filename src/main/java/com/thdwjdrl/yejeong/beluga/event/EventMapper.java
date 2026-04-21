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
			    title,
			    description,
			    start_at,
			    end_at,
			    winner_limit,
			    current_winner_count,
			    last_request_sequence,
			    status,
			    created_at
			) VALUES (
			    #{title},
			    #{description},
			    #{startAt},
			    #{endAt},
			    #{winnerLimit},
			    #{currentWinnerCount},
			    #{lastRequestSequence},
			    #{status},
			    #{createdAt}
			)
			""")
	@Options(useGeneratedKeys = true, keyProperty = "eventId")
	void insert(Event event);

	@Select("""
			SELECT
			    event_id,
			    title,
			    description,
			    start_at,
			    end_at,
			    winner_limit,
			    current_winner_count,
			    last_request_sequence,
			    status,
			    created_at
			FROM events
			WHERE event_id = #{eventId}
			""")
	Event findById(@Param("eventId") Long eventId);

	@Select("""
			SELECT
			    event_id,
			    title,
			    description,
			    start_at,
			    end_at,
			    winner_limit,
			    current_winner_count,
			    last_request_sequence,
			    status,
			    created_at
			FROM events
			WHERE event_id = #{eventId}
			FOR UPDATE
			""")
	Event findByIdForUpdate(@Param("eventId") Long eventId);

	@Select("""
			SELECT
			    event_id,
			    title,
			    description,
			    start_at,
			    end_at,
			    winner_limit,
			    current_winner_count,
			    last_request_sequence,
			    status,
			    created_at
			FROM events
			ORDER BY start_at DESC, event_id DESC
			""")
	List<Event> findAll();

	@Update("""
			UPDATE events
			SET current_winner_count = #{currentWinnerCount},
			    last_request_sequence = #{lastRequestSequence}
			WHERE event_id = #{eventId}
			""")
	int updateProgress(
			@Param("eventId") Long eventId,
			@Param("currentWinnerCount") int currentWinnerCount,
			@Param("lastRequestSequence") long lastRequestSequence
	);

}
