package com.thdwjdrl.yejeong.beluga.admin;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.thdwjdrl.yejeong.beluga.event.EventCreateRequest;
import com.thdwjdrl.yejeong.beluga.event.EventService;
import com.thdwjdrl.yejeong.beluga.event.EventSummaryResponse;
import com.thdwjdrl.yejeong.beluga.user.SessionUserService;
import com.thdwjdrl.yejeong.beluga.user.User;

import jakarta.servlet.http.HttpSession;

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
			@RequestPart("request") EventCreateRequest request,
			@RequestPart("image") MultipartFile image,
			HttpSession session
	) {
		User currentUser = sessionUserService.requireCurrentUser(session);
		return eventService.createEvent(request, image, currentUser);
	}

}
