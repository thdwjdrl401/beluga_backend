package com.thdwjdrl.yejeong.beluga.user;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.regex.Pattern;

import com.thdwjdrl.yejeong.beluga.common.exception.DuplicateEmailException;
import com.thdwjdrl.yejeong.beluga.common.exception.InvalidRequestException;
import com.thdwjdrl.yejeong.beluga.common.exception.ResourceNotFoundException;
import com.thdwjdrl.yejeong.beluga.common.exception.UnauthorizedException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

	private static final Pattern EMAIL_PATTERN =
			Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

	private final UserMapper userMapper;
	private final PasswordHasher passwordHasher;
	private final Clock clock;

	public UserService(UserMapper userMapper, PasswordHasher passwordHasher, Clock clock) {
		this.userMapper = userMapper;
		this.passwordHasher = passwordHasher;
		this.clock = clock;
	}

	@Transactional
	public User signUp(SignupRequest request) {
		validateSignupRequest(request);
		String normalizedEmail = normalizeAndValidateEmail(request.email());
		if (userMapper.findByEmail(normalizedEmail) != null) {
			throw new DuplicateEmailException("이미 가입된 이메일입니다.");
		}

		LocalDateTime now = LocalDateTime.now(clock);
		User user = new User();
		user.setEmail(normalizedEmail);
		user.setPasswordHash(passwordHasher.hash(validatePassword(request.password(), "비밀번호")));
		user.setNickname(validateNickname(request.nickname()));
		user.setRole(UserRole.USER);
		user.setCreatedAt(now);
		user.setUpdatedAt(now);
		try {
			userMapper.insert(user);
		}
		catch (DuplicateKeyException exception) {
			throw new DuplicateEmailException("이미 가입된 이메일입니다.");
		}
		return user;
	}

	@Transactional(readOnly = true)
	public User findByEmail(String normalizedEmail) {
		return userMapper.findByEmail(normalizedEmail);
	}

	@Transactional(readOnly = true)
	public User findById(Long userId) {
		return userMapper.findById(userId);
	}

	@Transactional(readOnly = true)
	public User getRequiredUser(Long userId) {
		User user = userMapper.findById(userId);
		if (user == null) {
			throw new ResourceNotFoundException("사용자를 찾을 수 없습니다.");
		}
		return user;
	}

	@Transactional(readOnly = true)
	public User authenticate(LoginRequest request) {
		validateLoginRequest(request);
		String normalizedEmail = normalizeAndValidateEmail(request.email());
		User user = userMapper.findByEmail(normalizedEmail);
		if (user == null || !passwordHasher.matches(request.password(), user.getPasswordHash())) {
			throw new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다.");
		}
		return user;
	}

	@Transactional
	public User updateNickname(Long userId, UpdateNicknameRequest request) {
		if (request == null) {
			throw new InvalidRequestException("닉네임 수정 요청이 비어 있습니다.");
		}
		userMapper.updateNickname(userId, validateNickname(request.nickname()), LocalDateTime.now(clock));
		return getRequiredUser(userId);
	}

	@Transactional
	public void updatePassword(Long userId, UpdatePasswordRequest request) {
		if (request == null) {
			throw new InvalidRequestException("비밀번호 수정 요청이 비어 있습니다.");
		}
		User user = getRequiredUser(userId);
		String currentPassword = validatePassword(request.currentPassword(), "현재 비밀번호");
		String newPassword = validatePassword(request.newPassword(), "새 비밀번호");
		if (!passwordHasher.matches(currentPassword, user.getPasswordHash())) {
			throw new UnauthorizedException("현재 비밀번호가 올바르지 않습니다.");
		}
		if (currentPassword.equals(newPassword)) {
			throw new InvalidRequestException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
		}
		userMapper.updatePassword(userId, passwordHasher.hash(newPassword), LocalDateTime.now(clock));
	}

	public UserProfileResponse toProfile(User user) {
		return new UserProfileResponse(
				user.getUserId(),
				user.getEmail(),
				user.getNickname(),
				user.getRole(),
				user.getCreatedAt(),
				user.getUpdatedAt()
		);
	}

	public String normalizeAndValidateEmail(String rawEmail) {
		if (rawEmail == null || rawEmail.isBlank()) {
			throw new InvalidRequestException("사용자 이메일이 필요합니다.");
		}

		String normalizedEmail = rawEmail.trim().toLowerCase(Locale.ROOT);
		if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
			throw new InvalidRequestException("사용자 이메일 형식이 올바르지 않습니다.");
		}
		if (normalizedEmail.length() > 255) {
			throw new InvalidRequestException("사용자 이메일은 255자를 초과할 수 없습니다.");
		}

		return normalizedEmail;
	}

	private void validateSignupRequest(SignupRequest request) {
		if (request == null) {
			throw new InvalidRequestException("회원가입 요청이 비어 있습니다.");
		}
	}

	private void validateLoginRequest(LoginRequest request) {
		if (request == null) {
			throw new InvalidRequestException("로그인 요청이 비어 있습니다.");
		}
		if (request.password() == null || request.password().isBlank()) {
			throw new InvalidRequestException("비밀번호는 필수입니다.");
		}
	}

	private String validatePassword(String rawPassword, String fieldName) {
		if (rawPassword == null || rawPassword.isBlank()) {
			throw new InvalidRequestException(fieldName + "는 필수입니다.");
		}
		if (rawPassword.length() < 8 || rawPassword.length() > 100) {
			throw new InvalidRequestException(fieldName + "는 8자 이상 100자 이하여야 합니다.");
		}
		return rawPassword;
	}

	private String validateNickname(String rawNickname) {
		if (rawNickname == null || rawNickname.isBlank()) {
			throw new InvalidRequestException("닉네임은 필수입니다.");
		}
		String nickname = rawNickname.trim();
		if (nickname.length() > 30) {
			throw new InvalidRequestException("닉네임은 30자를 초과할 수 없습니다.");
		}
		return nickname;
	}

}
