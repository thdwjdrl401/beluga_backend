package com.thdwjdrl.yejeong.beluga.event;

import java.util.List;

import com.thdwjdrl.yejeong.beluga.participation.ParticipationResponse;
import com.thdwjdrl.yejeong.beluga.participation.ParticipationResultResponse;
import com.thdwjdrl.yejeong.beluga.participation.ParticipationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events")
public class EventController {

	private final EventService eventService;
	private final ParticipationService participationService;

	public EventController(EventService eventService, ParticipationService participationService) {
		this.eventService = eventService;
		this.participationService = participationService;
	}

	@GetMapping
	public List<EventResponse> getEvents(@RequestParam(required = false) String status) {
		return eventService.getEvents(status);
	}

	@GetMapping("/{eventId}")
	public EventResponse getEvent(@PathVariable Long eventId) {
		return eventService.getEvent(eventId);
	}

	@PostMapping("/{eventId}/participate")
	public ParticipationResponse participate(
			@PathVariable Long eventId,
			@RequestHeader("X-User-Email") String userEmail
	) {
		return participationService.participate(eventId, userEmail);
	}

	@GetMapping("/{eventId}/result")
	public ParticipationResultResponse getResult(
			@PathVariable Long eventId,
			@RequestHeader("X-User-Email") String userEmail
	) {
		return participationService.getParticipationResult(eventId, userEmail);
	}

}
