package com.thdwjdrl.yejeong.beluga.user;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.regex.Pattern;

import com.thdwjdrl.yejeong.beluga.common.exception.InvalidRequestException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

	private static final Pattern EMAIL_PATTERN =
			Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

	private final UserMapper userMapper;
	private final Clock clock;

	public UserService(UserMapper userMapper, Clock clock) {
		this.userMapper = userMapper;
		this.clock = clock;
	}

	@Transactional
	public User findOrCreateByEmail(String normalizedEmail) {
		User existingUser = userMapper.findByEmail(normalizedEmail);
		if (existingUser != null) {
			return existingUser;
		}

		User user = new User();
		user.setEmail(normalizedEmail);
		user.setCreatedAt(LocalDateTime.now(clock));

		try {
			userMapper.insert(user);
			return user;
		}
		catch (DuplicateKeyException exception) {
			return userMapper.findByEmail(normalizedEmail);
		}
	}

	@Transactional(readOnly = true)
	public User findByEmail(String normalizedEmail) {
		return userMapper.findByEmail(normalizedEmail);
	}

	public String normalizeAndValidateEmail(String rawEmail) {
		if (rawEmail == null || rawEmail.isBlank()) {
			throw new InvalidRequestException("사용자 이메일이 필요합니다.");
		}

		String normalizedEmail = rawEmail.trim().toLowerCase(Locale.ROOT);
		if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
			throw new InvalidRequestException("사용자 이메일 형식이 올바르지 않습니다.");
		}

		return normalizedEmail;
	}

}
