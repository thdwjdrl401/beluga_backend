package com.thdwjdrl.yejeong.beluga.participation;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.thdwjdrl.yejeong.beluga.common.exception.ResourceNotFoundException;
import com.thdwjdrl.yejeong.beluga.event.Event;
import com.thdwjdrl.yejeong.beluga.event.EventMapper;
import com.thdwjdrl.yejeong.beluga.event.EventService;
import com.thdwjdrl.yejeong.beluga.event.EventStatus;
import com.thdwjdrl.yejeong.beluga.event.EventStatusResolver;
import com.thdwjdrl.yejeong.beluga.user.User;
import com.thdwjdrl.yejeong.beluga.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParticipationService {

	private static final Logger log = LoggerFactory.getLogger(ParticipationService.class);

	private final EventMapper eventMapper;
	private final EventService eventService;
	private final EventStatusResolver eventStatusResolver;
	private final UserService userService;
	private final ParticipationMapper participationMapper;
	private final Clock clock;

	public ParticipationService(
			EventMapper eventMapper,
			EventService eventService,
			EventStatusResolver eventStatusResolver,
			UserService userService,
			ParticipationMapper participationMapper,
			Clock clock
	) {
		this.eventMapper = eventMapper;
		this.eventService = eventService;
		this.eventStatusResolver = eventStatusResolver;
		this.userService = userService;
		this.participationMapper = participationMapper;
		this.clock = clock;
	}

	@Transactional
	public ParticipationResponse participate(Long eventId, String rawUserEmail) {
		String requestId = UUID.randomUUID().toString();
		String userEmail = userService.normalizeAndValidateEmail(rawUserEmail);
		Event event = eventMapper.findByIdForUpdate(eventId);
		if (event == null) {
			throw new ResourceNotFoundException("이벤트를 찾을 수 없습니다.");
		}

		EventStatus status = eventStatusResolver.resolve(event);
		if (status == EventStatus.SCHEDULED) {
			log.info("participation_rejected requestId={} eventId={} reason=BEFORE_START", requestId, eventId);
			return new ParticipationResponse(
					eventId,
					null,
					ParticipationResultStatus.BEFORE_START,
					null,
					null,
					null,
					requestId,
					"이벤트 시작 전입니다."
			);
		}
		if (status == EventStatus.ENDED) {
			log.info("participation_rejected requestId={} eventId={} reason=ENDED", requestId, eventId);
			return new ParticipationResponse(
					eventId,
					null,
					ParticipationResultStatus.ENDED,
					null,
					null,
					null,
					requestId,
					"이벤트가 종료되었습니다."
			);
		}

		User user = userService.findOrCreateByEmail(userEmail);
		Participation existing = participationMapper.findByEventIdAndUserId(eventId, user.getUserId());
		if (existing != null) {
			log.info(
					"participation_duplicate requestId={} eventId={} userId={} existingResult={}",
					requestId,
					eventId,
					user.getUserId(),
					existing.getResultStatus()
			);
			return new ParticipationResponse(
					eventId,
					user.getUserId(),
					ParticipationResultStatus.DUPLICATE,
					existing.getResultStatus(),
					existing.getRequestSequence(),
					existing.getParticipatedAt(),
					requestId,
					"이미 참여한 이벤트입니다."
			);
		}

		long requestSequence = event.getLastRequestSequence() + 1;
		ParticipationResultStatus resultStatus = event.getCurrentWinnerCount() < event.getWinnerLimit()
				? ParticipationResultStatus.WIN
				: ParticipationResultStatus.LOSE;
		int currentWinnerCount = resultStatus == ParticipationResultStatus.WIN
				? event.getCurrentWinnerCount() + 1
				: event.getCurrentWinnerCount();

		eventMapper.updateProgress(eventId, currentWinnerCount, requestSequence);

		LocalDateTime now = LocalDateTime.now(clock);
		Participation participation = new Participation();
		participation.setEventId(eventId);
		participation.setUserId(user.getUserId());
		participation.setParticipatedAt(now);
		participation.setCreatedAt(now);
		participation.setRequestSequence(requestSequence);
		participation.setResultStatus(resultStatus);

		participationMapper.insert(participation);

		log.info(
				"participation_completed requestId={} eventId={} userId={} result={} requestSequence={}",
				requestId,
				eventId,
				user.getUserId(),
				resultStatus,
				requestSequence
		);

		return new ParticipationResponse(
				eventId,
				user.getUserId(),
				resultStatus,
				null,
				requestSequence,
				now,
				requestId,
				resultStatus == ParticipationResultStatus.WIN ? "당첨되었습니다." : "당첨 인원이 마감되어 미당첨 처리되었습니다."
		);
	}

	@Transactional(readOnly = true)
	public ParticipationResultResponse getParticipationResult(Long eventId, String rawUserEmail) {
		eventService.getRequiredEvent(eventId);
		String userEmail = userService.normalizeAndValidateEmail(rawUserEmail);
		User user = userService.findByEmail(userEmail);
		if (user == null) {
			return new ParticipationResultResponse(eventId, null, false, null, null, null);
		}

		Participation participation = participationMapper.findByEventIdAndUserId(eventId, user.getUserId());
		if (participation == null) {
			return new ParticipationResultResponse(eventId, user.getUserId(), false, null, null, null);
		}

		return new ParticipationResultResponse(
				eventId,
				user.getUserId(),
				true,
				participation.getResultStatus(),
				participation.getRequestSequence(),
				participation.getParticipatedAt()
		);
	}

	@Transactional(readOnly = true)
	public List<ParticipantResponse> getParticipants(Long eventId) {
		eventService.getRequiredEvent(eventId);
		return participationMapper.findParticipantsByEventId(eventId).stream()
				.map(this::toParticipantResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<ParticipantResponse> getWinners(Long eventId) {
		eventService.getRequiredEvent(eventId);
		return participationMapper.findWinnersByEventId(eventId).stream()
				.map(this::toParticipantResponse)
				.toList();
	}

	private ParticipantResponse toParticipantResponse(ParticipationHistoryRow row) {
		return new ParticipantResponse(
				row.getUserId(),
				row.getUserEmail(),
				row.getResultStatus(),
				row.getRequestSequence(),
				row.getParticipatedAt()
		);
	}

}
