package com.thdwjdrl.yejeong.beluga.event;

import java.util.List;

import com.thdwjdrl.yejeong.beluga.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventService {

	private final EventMapper eventMapper;
	private final EventStatusResolver eventStatusResolver;

	public EventService(EventMapper eventMapper, EventStatusResolver eventStatusResolver) {
		this.eventMapper = eventMapper;
		this.eventStatusResolver = eventStatusResolver;
	}

	@Transactional(readOnly = true)
	public EventListResponse getVisibleEvents() {
		List<EventSummaryResponse> visibleEvents = eventMapper.findAll().stream()
				.map(this::toSummaryResponse)
				.filter(event -> event.status() != EventStatus.ENDED)
				.sorted((left, right) -> {
					int statusCompare = Integer.compare(priorityOf(left.status()), priorityOf(right.status()));
					if (statusCompare != 0) {
						return statusCompare;
					}
					return left.startAt().compareTo(right.startAt());
				})
				.toList();

		return new EventListResponse(
				visibleEvents.stream()
						.filter(event -> event.status() == EventStatus.ACTIVE)
						.toList(),
				visibleEvents.stream()
						.filter(event -> event.status() == EventStatus.SCHEDULED)
						.toList()
		);
	}

	@Transactional(readOnly = true)
	public Event getRequiredEvent(Long eventId) {
		Event event = eventMapper.findById(eventId);
		if (event == null) {
			throw new ResourceNotFoundException("이벤트를 찾을 수 없습니다.");
		}
		return event;
	}

	private int priorityOf(EventStatus status) {
		return switch (status) {
			case ACTIVE -> 0;
			case SCHEDULED -> 1;
			case ENDED -> 2;
		};
	}

	private EventSummaryResponse toSummaryResponse(Event event) {
		return new EventSummaryResponse(
				event.getEventId(),
				event.getEventName(),
				event.getProductName(),
				event.getRepresentativeAttachId(),
				event.getWinnerLimit(),
				event.getWinnerCount(),
				event.getParticipantCount(),
				event.getStartAt(),
				event.getEndAt(),
				eventStatusResolver.resolve(event)
		);
	}

}
