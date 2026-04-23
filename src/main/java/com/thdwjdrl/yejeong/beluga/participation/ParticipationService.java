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
	private final ParticipationMapper participationMapper;
	private final Clock clock;

	public ParticipationService(
			EventMapper eventMapper,
			EventService eventService,
			EventStatusResolver eventStatusResolver,
			ParticipationMapper participationMapper,
			Clock clock
	) {
		this.eventMapper = eventMapper;
		this.eventService = eventService;
		this.eventStatusResolver = eventStatusResolver;
		this.participationMapper = participationMapper;
		this.clock = clock;
	}

	@Transactional
	public ParticipationResponse participate(Long eventId, Long userId) {
		String requestId = UUID.randomUUID().toString();
		Event event = eventMapper.findByIdForUpdate(eventId);
		if (event == null) {
			throw new ResourceNotFoundException("이벤트를 찾을 수 없습니다.");
		}

		EventStatus status = eventStatusResolver.resolve(event);
		if (status == EventStatus.SCHEDULED) {
			log.info("participation_rejected requestId={} eventId={} reason=BEFORE_START", requestId, eventId);
			return new ParticipationResponse(
					eventId,
					userId,
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
					userId,
					ParticipationResultStatus.ENDED,
					null,
					null,
					null,
					requestId,
					"이벤트가 종료되었습니다."
			);
		}

		Participation existing = participationMapper.findByEventIdAndUserId(eventId, userId);
		if (existing != null) {
			log.info(
					"participation_duplicate requestId={} eventId={} userId={} existingResult={}",
					requestId,
					eventId,
					userId,
					existing.getResultStatus()
			);
			return new ParticipationResponse(
					eventId,
					userId,
					ParticipationResultStatus.DUPLICATE,
					existing.getResultStatus(),
					existing.getRequestSequence(),
					existing.getParticipatedAt(),
					requestId,
					"이미 참여한 이벤트입니다."
			);
		}

		long requestSequence = (long) event.getParticipantCount() + 1;
		ParticipationResultStatus resultStatus = event.getWinnerCount() < event.getWinnerLimit()
				? ParticipationResultStatus.WIN
				: ParticipationResultStatus.LOSE;
		int winnerCount = resultStatus == ParticipationResultStatus.WIN
				? event.getWinnerCount() + 1
				: event.getWinnerCount();
		int participantCount = event.getParticipantCount() + 1;

		LocalDateTime now = LocalDateTime.now(clock);
		eventMapper.updateProgress(eventId, winnerCount, participantCount, now);

		Participation participation = new Participation();
		participation.setEventId(eventId);
		participation.setUserId(userId);
		participation.setParticipatedAt(now);
		participation.setRequestSequence(requestSequence);
		participation.setResultStatus(resultStatus);
		participation.setGifticonAttachId(null);
		participation.setCreatedAt(now);

		participationMapper.insert(participation);

		log.info(
				"participation_completed requestId={} eventId={} userId={} result={} requestSequence={}",
				requestId,
				eventId,
				userId,
				resultStatus,
				requestSequence
		);

		return new ParticipationResponse(
				eventId,
				userId,
				resultStatus,
				null,
				requestSequence,
				now,
				requestId,
				resultStatus == ParticipationResultStatus.WIN ? "당첨되었습니다." : "당첨 인원이 마감되어 미당첨 처리되었습니다."
		);
	}

	@Transactional(readOnly = true)
	public List<MyParticipationResponse> getMyParticipations(Long userId) {
		return participationMapper.findMyParticipationsByUserId(userId).stream()
				.map(row -> new MyParticipationResponse(
						row.getEventId(),
						row.getEventName(),
						row.getProductName(),
						row.getResultStatus(),
						row.getParticipatedAt(),
						row.getStartAt(),
						row.getEndAt(),
						row.getGifticonAttachId() != null
				))
				.toList();
	}

	@Transactional(readOnly = true)
	public Long getGifticonAttachId(Long userId, Long eventId) {
		eventService.getRequiredEvent(eventId);
		Participation participation = participationMapper.findByEventIdAndUserId(eventId, userId);
		if (participation == null || participation.getResultStatus() != ParticipationResultStatus.WIN
				|| participation.getGifticonAttachId() == null) {
			throw new ResourceNotFoundException("기프티콘 이미지를 찾을 수 없습니다.");
		}
		return participation.getGifticonAttachId();
	}

}
