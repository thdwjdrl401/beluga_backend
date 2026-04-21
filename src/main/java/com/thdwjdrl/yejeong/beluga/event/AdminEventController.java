package com.thdwjdrl.yejeong.beluga.event;

import java.util.List;

import com.thdwjdrl.yejeong.beluga.participation.ParticipantResponse;
import com.thdwjdrl.yejeong.beluga.participation.ParticipationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/events")
public class AdminEventController {

	private final EventService eventService;
	private final ParticipationService participationService;

	public AdminEventController(EventService eventService, ParticipationService participationService) {
		this.eventService = eventService;
		this.participationService = participationService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public EventResponse createEvent(@RequestBody CreateEventRequest request) {
		return eventService.createEvent(request);
	}

	@GetMapping("/{eventId}")
	public EventResponse getEvent(@PathVariable Long eventId) {
		return eventService.getEvent(eventId);
	}

	@GetMapping("/{eventId}/participants")
	public List<ParticipantResponse> getParticipants(@PathVariable Long eventId) {
		return participationService.getParticipants(eventId);
	}

	@GetMapping("/{eventId}/winners")
	public List<ParticipantResponse> getWinners(@PathVariable Long eventId) {
		return participationService.getWinners(eventId);
	}

}
