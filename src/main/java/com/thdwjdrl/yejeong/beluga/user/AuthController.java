package com.thdwjdrl.yejeong.beluga.user;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final UserService userService;
	private final SessionUserService sessionUserService;

	public AuthController(UserService userService, SessionUserService sessionUserService) {
		this.userService = userService;
		this.sessionUserService = sessionUserService;
	}

	@PostMapping("/signup")
	@ResponseStatus(HttpStatus.CREATED)
	public UserProfileResponse signup(@RequestBody SignupRequest request) {
		return userService.toProfile(userService.signUp(request));
	}

	@PostMapping("/login")
	public UserProfileResponse login(@RequestBody LoginRequest request, HttpSession session) {
		User user = userService.authenticate(request);
		sessionUserService.login(session, user);
		return userService.toProfile(user);
	}

}
