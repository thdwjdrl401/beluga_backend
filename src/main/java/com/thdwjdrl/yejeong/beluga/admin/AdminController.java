package com.thdwjdrl.yejeong.beluga.admin;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.thdwjdrl.yejeong.beluga.event.EventCreateRequest;
import com.thdwjdrl.yejeong.beluga.event.EventService;
import com.thdwjdrl.yejeong.beluga.event.EventSummaryResponse;
import com.thdwjdrl.yejeong.beluga.user.SessionUserService;
import com.thdwjdrl.yejeong.beluga.user.User;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin")
public class AdminController {

	private final EventService eventService;
	private final SessionUserService sessionUserService;

	public AdminController(EventService eventService, SessionUserService sessionUserService) {
		this.eventService = eventService;
		this.sessionUserService = sessionUserService;
	}

	@PostMapping(path = "/events", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public EventSummaryResponse createEvent(
			@RequestParam("eventName") String eventName,
			@RequestParam("productName") String productName,
			@RequestParam("winnerLimit") int winnerLimit,
			@RequestParam("startAt") String startAt,
			@RequestParam("endAt") String endAt,
			@RequestPart(value = "image", required = false) MultipartFile image,
			HttpSession session
	) {
		User currentUser = sessionUserService.requireCurrentUser(session);

		EventCreateRequest request = new EventCreateRequest(
				eventName,
				productName,
				LocalDateTime.parse(startAt),
				LocalDateTime.parse(endAt),
				winnerLimit
		);
		return eventService.createEvent(request, image, currentUser);
	}

}
