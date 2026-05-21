package com.thdwjdrl.yejeong.beluga.admin;

import com.thdwjdrl.yejeong.beluga.common.api.SuccessResponse;
import com.thdwjdrl.yejeong.beluga.event.*;
import com.thdwjdrl.yejeong.beluga.participation.ParticipationService;
import com.thdwjdrl.yejeong.beluga.participation.ParticipationsListResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.thdwjdrl.yejeong.beluga.user.SessionUserService;
import com.thdwjdrl.yejeong.beluga.user.User;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {

	private final EventService eventService;
	private final SessionUserService sessionUserService;
	private final ParticipationService participationService;

	public AdminController(EventService eventService, SessionUserService sessionUserService, ParticipationService participationService) {
		this.eventService = eventService;
		this.sessionUserService = sessionUserService;
		this.participationService = participationService;
	}

	@PostMapping(path = "/events", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public SuccessResponse createEvent(
			@RequestParam("eventName") String eventName,
			@RequestParam("productName") String productName,
			@RequestParam("winnerLimit") int winnerLimit,
			@RequestParam("startAt") String startAt,
			@RequestParam("endAt") String endAt,
			@RequestPart("image") MultipartFile image,
			HttpSession session
	) {
		User currentUser = sessionUserService.requireCurrentUser(session);

		log.info(
				"admin_create_event_session_check sessionId={} userId={} role={}",
				session.getId(),
				currentUser.getUserId(),
				currentUser.getRole()
		);

		EventCreateRequest request = new EventCreateRequest(
				eventName,
				productName,
				LocalDateTime.parse(startAt),
				LocalDateTime.parse(endAt),
				winnerLimit
		);

		eventService.createEvent(request, image, currentUser);

		return SuccessResponse.success("이벤트가 생성되었습니다.", null);
	}

	@GetMapping(path = "/events/results")
	public SuccessResponse<List<EventResultResponse>> getEventsResult(
		@RequestParam("status") EventStatus status){

		List<EventResultResponse> response = eventService.getEventsByStatus(status);
		return SuccessResponse.success(response);
	}

	@GetMapping(path = "/events/{eventId}/participations")
	public SuccessResponse<List<ParticipationsListResponse>> getParticipationsList(
			@PathVariable("eventId") long eventId
	){
		List<ParticipationsListResponse> response = participationService.getAllParticipations(eventId);
		return SuccessResponse.success(response);
	}





}
