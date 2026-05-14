package com.thdwjdrl.yejeong.beluga.user;

import com.thdwjdrl.yejeong.beluga.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SessionUserService {

	private static final String USER_ID_SESSION_KEY = "belugaUserId";

	private final UserService userService;

	public SessionUserService(UserService userService) {
		this.userService = userService;
	}

	public void login(HttpSession session, User user) {
		session.setAttribute(USER_ID_SESSION_KEY, user.getUserId());
	}

	public User requireCurrentUser(HttpSession session) {
		Object userId = session.getAttribute(USER_ID_SESSION_KEY);
		log.info("session_user_lookup sessionId={} userId={}", session.getId(), userId);
		if (!(userId instanceof Number numberValue)) {
			throw new UnauthorizedException("로그인이 필요합니다.");
		}

		User user = userService.findById(numberValue.longValue());
		if (user == null) {
			session.invalidate();
			throw new UnauthorizedException("유효하지 않은 로그인 상태입니다.");
		}
		return user;
	}

}
