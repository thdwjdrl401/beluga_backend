package com.thdwjdrl.yejeong.beluga.event;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import com.thdwjdrl.yejeong.beluga.common.exception.InvalidRequestException;
import com.thdwjdrl.yejeong.beluga.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventService {

	private final EventMapper eventMapper;
	private final EventStatusResolver eventStatusResolver;
	private final Clock clock;

	public EventService(EventMapper eventMapper, EventStatusResolver eventStatusResolver, Clock clock) {
		this.eventMapper = eventMapper;
		this.eventStatusResolver = eventStatusResolver;
		this.clock = clock;
	}

	@Transactional
	public EventResponse createEvent(CreateEventRequest request) {
		validateCreateRequest(request);

		LocalDateTime now = LocalDateTime.now(clock);
		Event event = new Event();
		event.setTitle(request.title().trim());
		event.setDescription(request.description().trim());
		event.setStartAt(request.startAt());
		event.setEndAt(request.endAt());
		event.setWinnerLimit(request.winnerLimit());
		event.setCurrentWinnerCount(0);
		event.setLastRequestSequence(0);
		event.setCreatedAt(now);
		event.setStatus(eventStatusResolver.resolve(request.startAt(), request.endAt()));

		eventMapper.insert(event);
		return toResponse(event);
	}

	@Transactional(readOnly = true)
	public List<EventResponse> getEvents(String statusText) {
		EventStatus statusFilter = parseStatus(statusText);
		return eventMapper.findAll().stream()
				.map(this::toResponse)
				.filter(event -> statusFilter == null || event.status() == statusFilter)
				.toList();
	}

	@Transactional(readOnly = true)
	public EventResponse getEvent(Long eventId) {
		return toResponse(getRequiredEvent(eventId));
	}

	@Transactional(readOnly = true)
	public Event getRequiredEvent(Long eventId) {
		Event event = eventMapper.findById(eventId);
		if (event == null) {
			throw new ResourceNotFoundException("이벤트를 찾을 수 없습니다.");
		}
		return event;
	}

	private void validateCreateRequest(CreateEventRequest request) {
		if (request == null) {
			throw new InvalidRequestException("이벤트 생성 요청이 비어 있습니다.");
		}
		if (request.title() == null || request.title().isBlank()) {
			throw new InvalidRequestException("이벤트명은 필수입니다.");
		}
		if (request.title().length() > 100) {
			throw new InvalidRequestException("이벤트명은 100자를 초과할 수 없습니다.");
		}
		if (request.description() == null || request.description().isBlank()) {
			throw new InvalidRequestException("이벤트 설명은 필수입니다.");
		}
		if (request.description().length() > 500) {
			throw new InvalidRequestException("이벤트 설명은 500자를 초과할 수 없습니다.");
		}
		if (request.startAt() == null || request.endAt() == null) {
			throw new InvalidRequestException("이벤트 시작/종료 시간은 필수입니다.");
		}
		if (!request.startAt().isBefore(request.endAt())) {
			throw new InvalidRequestException("이벤트 종료 시간은 시작 시간보다 뒤여야 합니다.");
		}
		if (request.winnerLimit() <= 0) {
			throw new InvalidRequestException("최대 당첨 인원은 1명 이상이어야 합니다.");
		}
	}

	private EventStatus parseStatus(String statusText) {
		if (statusText == null || statusText.isBlank()) {
			return null;
		}

		try {
			return EventStatus.valueOf(statusText.trim().toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException exception) {
			throw new InvalidRequestException("지원하지 않는 이벤트 상태입니다.");
		}
	}

	private EventResponse toResponse(Event event) {
		return new EventResponse(
				event.getEventId(),
				event.getTitle(),
				event.getDescription(),
				event.getStartAt(),
				event.getEndAt(),
				event.getWinnerLimit(),
				event.getCurrentWinnerCount(),
				eventStatusResolver.resolve(event),
				event.getCreatedAt()
		);
	}

}
