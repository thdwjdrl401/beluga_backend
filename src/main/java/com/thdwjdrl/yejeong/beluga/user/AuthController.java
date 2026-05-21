package com.thdwjdrl.yejeong.beluga.user;

import com.thdwjdrl.yejeong.beluga.common.api.SuccessResponse;
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
	public SuccessResponse<UserProfileResponse> signup(@RequestBody SignupRequest request) {
		return SuccessResponse.success(
				"회원가입이 완료되었습니다.",
				userService.toProfile(userService.signUp(request))
		);
	}

	@PostMapping("/login")
	public SuccessResponse<UserProfileResponse> login(@RequestBody LoginRequest request, HttpSession session) {
		User user = userService.authenticate(request);
		sessionUserService.login(session, user);
		return SuccessResponse.success("로그인이 완료되었습니다.", userService.toProfile(user));
	}

}
