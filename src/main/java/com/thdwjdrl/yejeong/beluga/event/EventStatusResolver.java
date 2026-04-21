package com.thdwjdrl.yejeong.beluga.event;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

@Component
public class EventStatusResolver {

	private final Clock clock;

	public EventStatusResolver(Clock clock) {
		this.clock = clock;
	}

	public EventStatus resolve(Event event) {
		return resolve(event.getStartAt(), event.getEndAt());
	}

	public EventStatus resolve(LocalDateTime startAt, LocalDateTime endAt) {
		LocalDateTime now = LocalDateTime.now(clock);
		if (now.isBefore(startAt)) {
			return EventStatus.SCHEDULED;
		}
		if (!now.isBefore(endAt)) {
			return EventStatus.ENDED;
		}
		return EventStatus.ACTIVE;
	}

}
