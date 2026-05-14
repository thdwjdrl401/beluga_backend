package com.thdwjdrl.yejeong.beluga.event;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import com.thdwjdrl.yejeong.beluga.attach.Attach;
import com.thdwjdrl.yejeong.beluga.attach.AttachService;
import com.thdwjdrl.yejeong.beluga.common.exception.ResourceNotFoundException;
import com.thdwjdrl.yejeong.beluga.common.exception.UnauthorizedException;
import com.thdwjdrl.yejeong.beluga.user.User;

@Service
@Slf4j
public class EventService {

	private final EventMapper eventMapper;
	private final EventStatusResolver eventStatusResolver;
	private final AttachService attachService;
	private final Clock clock;

	public EventService(
			EventMapper eventMapper,
			EventStatusResolver eventStatusResolver,
			AttachService attachService,
			Clock clock
	) {
		this.eventMapper = eventMapper;
		this.eventStatusResolver = eventStatusResolver;
		this.attachService = attachService;
		this.clock = clock;
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

	@Transactional
	public EventSummaryResponse createEvent(EventCreateRequest request, MultipartFile image, User currentUser) {
		
		if (!"ADMIN".equals(currentUser.getRole().name())) {
			throw new UnauthorizedException("관리자만 이벤트를 생성할 수 있습니다.");
		}

		LocalDateTime now = LocalDateTime.now(clock);
		Attach representativeAttach = attachService.saveEventRepresentativeImage(image);

		try {
			Event event = new Event();
			event.setEventName(request.eventName());
			event.setProductName(request.productName());
			event.setRepresentativeAttachId(representativeAttach.getAttachId());
			event.setStartAt(request.startAt());
			event.setEndAt(request.endAt());
			event.setWinnerLimit(request.winnerLimit());
			event.setWinnerCount(0);
			event.setParticipantCount(0);
			event.setStatus(eventStatusResolver.resolve(event));
			event.setCreatedBy(currentUser.getUserId());
			event.setCreatedAt(now);
			event.setUpdatedAt(now);

			eventMapper.insert(event);

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
		catch (RuntimeException exception) {
			attachService.deleteStoredFile(representativeAttach);
			throw exception;
		}
	}

}
