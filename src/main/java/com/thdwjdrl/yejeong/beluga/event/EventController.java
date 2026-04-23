package com.thdwjdrl.yejeong.beluga.event;

import com.thdwjdrl.yejeong.beluga.participation.ParticipationResponse;
import com.thdwjdrl.yejeong.beluga.participation.ParticipationService;
import com.thdwjdrl.yejeong.beluga.user.SessionUserService;
import com.thdwjdrl.yejeong.beluga.user.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events")
public class EventController {

	private final EventService eventService;
	private final ParticipationService participationService;
	private final SessionUserService sessionUserService;

	public EventController(
			EventService eventService,
			ParticipationService participationService,
			SessionUserService sessionUserService
	) {
		this.eventService = eventService;
		this.participationService = participationService;
		this.sessionUserService = sessionUserService;
	}

	@GetMapping
	public EventListResponse getEvents() {
		return eventService.getVisibleEvents();
	}

	@PostMapping("/{eventId}/participate")
	public ParticipationResponse participate(
			@PathVariable Long eventId,
			HttpSession session
	) {
		User currentUser = sessionUserService.requireCurrentUser(session);
		return participationService.participate(eventId, currentUser.getUserId());
	}

}
