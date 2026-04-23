package com.thdwjdrl.yejeong.beluga;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import com.thdwjdrl.yejeong.beluga.participation.ParticipationResponse;
import com.thdwjdrl.yejeong.beluga.participation.ParticipationResultStatus;
import com.thdwjdrl.yejeong.beluga.participation.ParticipationService;
import com.thdwjdrl.yejeong.beluga.user.LoginRequest;
import com.thdwjdrl.yejeong.beluga.user.SignupRequest;
import com.thdwjdrl.yejeong.beluga.user.User;
import com.thdwjdrl.yejeong.beluga.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BelugaUserIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ParticipationService participationService;

	@Autowired
	private UserService userService;

	@TempDir
	Path tempDir;

	@BeforeEach
	void setUp() {
		jdbcTemplate.update("DELETE FROM event_participations");
		jdbcTemplate.update("DELETE FROM events");
		jdbcTemplate.update("DELETE FROM attach");
		jdbcTemplate.update("DELETE FROM users");
	}

	@Test
	void signupLoginAndMeFlowWorks() throws Exception {
		mockMvc.perform(post("/auth/signup")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "user@example.com",
								  "password": "password123",
								  "nickname": "벨루가"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value("user@example.com"))
				.andExpect(jsonPath("$.nickname").value("벨루가"))
				.andExpect(jsonPath("$.role").value("USER"));

		MockHttpSession session = login("user@example.com", "password123");

		mockMvc.perform(get("/me").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("user@example.com"))
				.andExpect(jsonPath("$.nickname").value("벨루가"));

		mockMvc.perform(patch("/me/nickname").session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "nickname": "새닉네임"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nickname").value("새닉네임"));

		mockMvc.perform(patch("/me/password").session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "currentPassword": "password123",
								  "newPassword": "password456"
								}
								"""))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(asJson(new LoginRequest("user@example.com", "password123"))))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(asJson(new LoginRequest("user@example.com", "password456"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nickname").value("새닉네임"));
	}

	@Test
	void eventsEndpointReturnsOnlyActiveAndScheduledSections() throws Exception {
		LocalDateTime now = LocalDateTime.now().withNano(0);
		insertEvent("진행 이벤트", "상품A", now.minusMinutes(10), now.plusMinutes(10), 5);
		insertEvent("예정 이벤트", "상품B", now.plusMinutes(10), now.plusMinutes(30), 5);
		insertEvent("종료 이벤트", "상품C", now.minusMinutes(30), now.minusMinutes(10), 5);

		mockMvc.perform(get("/events"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.activeEvents.length()").value(1))
				.andExpect(jsonPath("$.scheduledEvents.length()").value(1))
				.andExpect(jsonPath("$.activeEvents[0].eventName").value("진행 이벤트"))
				.andExpect(jsonPath("$.activeEvents[0].status").value("ACTIVE"))
				.andExpect(jsonPath("$.scheduledEvents[0].eventName").value("예정 이벤트"))
				.andExpect(jsonPath("$.scheduledEvents[0].status").value("SCHEDULED"));
	}

	@Test
	void unauthenticatedEndpointsRequireLogin() throws Exception {
		long eventId = insertActiveEvent("로그인 필요 이벤트", 2);

		mockMvc.perform(get("/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

		mockMvc.perform(post("/events/{eventId}/participate", eventId))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}

	@Test
	void participationAndMyPageFlowWorks() throws Exception {
		long eventId = insertActiveEvent("참여 이벤트", 1);
		MockHttpSession session = signUpAndLogin("winner@example.com", "password123", "당첨자");

		mockMvc.perform(post("/events/{eventId}/participate", eventId).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultStatus").value("WIN"))
				.andExpect(jsonPath("$.requestSequence").value(1L));

		mockMvc.perform(post("/events/{eventId}/participate", eventId).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultStatus").value("DUPLICATE"))
				.andExpect(jsonPath("$.existingResultStatus").value("WIN"));

		mockMvc.perform(get("/me/participations").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].eventId").value(eventId))
				.andExpect(jsonPath("$[0].eventName").value("참여 이벤트"))
				.andExpect(jsonPath("$[0].resultStatus").value("WIN"))
				.andExpect(jsonPath("$[0].gifticonAvailable").value(false));
	}

	@Test
	void participateReturnsBeforeStartAndEnded() throws Exception {
		LocalDateTime now = LocalDateTime.now().withNano(0);
		long scheduledEventId = insertEvent("예정 이벤트", "상품A", now.plusMinutes(1), now.plusMinutes(10), 1);
		long endedEventId = insertEvent("종료 이벤트", "상품B", now.minusMinutes(10), now.minusMinutes(1), 1);
		MockHttpSession session = signUpAndLogin("user@example.com", "password123", "사용자");

		mockMvc.perform(post("/events/{eventId}/participate", scheduledEventId).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultStatus").value("BEFORE_START"));

		mockMvc.perform(post("/events/{eventId}/participate", endedEventId).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultStatus").value("ENDED"));
	}

	@Test
	void winnerGifticonCanBeViewedAndDownloaded() throws Exception {
		long eventId = insertActiveEvent("기프티콘 이벤트", 1);
		MockHttpSession session = signUpAndLogin("gifticon@example.com", "password123", "기프티콘유저");

		mockMvc.perform(post("/events/{eventId}/participate", eventId).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultStatus").value("WIN"));

		User user = userService.findByEmail("gifticon@example.com");
		Path gifticonPath = tempDir.resolve("gifticon.png");
		byte[] gifticonBytes = "gifticon-image".getBytes();
		Files.write(gifticonPath, gifticonBytes);
		long attachId = insertAttach("GIFTICON_IMAGE", "gifticon.png", "gifticon-stored.png", gifticonPath, "image/png");
		jdbcTemplate.update("""
				UPDATE event_participations
				SET gifticon_attach_id = ?
				WHERE event_id = ? AND user_id = ?
				""", attachId, eventId, user.getUserId());

		mockMvc.perform(get("/me/participations").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].gifticonAvailable").value(true));

		mockMvc.perform(get("/me/participations/{eventId}/gifticon", eventId).session(session))
				.andExpect(status().isOk())
				.andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("inline")))
				.andExpect(content().contentType("image/png"))
				.andExpect(content().bytes(gifticonBytes));

		mockMvc.perform(get("/me/participations/{eventId}/gifticon/download", eventId).session(session))
				.andExpect(status().isOk())
				.andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
				.andExpect(content().bytes(gifticonBytes));
	}

	@Test
	void concurrentParticipationKeepsWinnerLimitAndSequenceOrder() throws Exception {
		long eventId = insertActiveEvent("동시성 이벤트", 5);
		List<User> users = IntStream.range(0, 20)
				.mapToObj(index -> userService.signUp(new SignupRequest(
						"user" + index + "@example.com",
						"password123",
						"유저" + index
				)))
				.toList();

		ExecutorService executorService = Executors.newFixedThreadPool(20);
		CountDownLatch readyLatch = new CountDownLatch(20);
		CountDownLatch startLatch = new CountDownLatch(1);

		try {
			List<Future<ParticipationResponse>> futures = users.stream()
					.map(user -> executorService.submit(() -> {
						readyLatch.countDown();
						startLatch.await(5, TimeUnit.SECONDS);
						return participationService.participate(eventId, user.getUserId());
					}))
					.toList();

			assertThat(readyLatch.await(5, TimeUnit.SECONDS)).isTrue();
			startLatch.countDown();

			List<ParticipationResponse> responses = futures.stream()
					.map(future -> {
						try {
							return future.get(10, TimeUnit.SECONDS);
						}
						catch (Exception exception) {
							throw new IllegalStateException(exception);
						}
					})
					.toList();

			assertThat(responses.stream()
					.filter(response -> response.resultStatus() == ParticipationResultStatus.WIN)
					.count()).isEqualTo(5);
			assertThat(responses.stream()
					.filter(response -> response.resultStatus() == ParticipationResultStatus.LOSE)
					.count()).isEqualTo(15);

			List<Long> sequences = jdbcTemplate.queryForList("""
					SELECT request_sequence
					FROM event_participations
					WHERE event_id = ?
					ORDER BY request_sequence ASC
					""", Long.class, eventId);

			assertThat(sequences).containsExactlyElementsOf(LongStream.rangeClosed(1, 20).boxed().toList());
			assertThat(jdbcTemplate.queryForObject(
					"SELECT winner_count FROM events WHERE event_id = ?",
					Integer.class,
					eventId
			)).isEqualTo(5);
			assertThat(jdbcTemplate.queryForObject(
					"SELECT participant_count FROM events WHERE event_id = ?",
					Integer.class,
					eventId
			)).isEqualTo(20);
		}
		finally {
			executorService.shutdownNow();
		}
	}

	private MockHttpSession signUpAndLogin(String email, String password, String nickname) throws Exception {
		mockMvc.perform(post("/auth/signup")
						.contentType(MediaType.APPLICATION_JSON)
						.content(asJson(new SignupRequest(email, password, nickname))))
				.andExpect(status().isCreated());
		return login(email, password);
	}

	private MockHttpSession login(String email, String password) throws Exception {
		return (MockHttpSession) mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(asJson(new LoginRequest(email, password))))
				.andExpect(status().isOk())
				.andReturn()
				.getRequest()
				.getSession(false);
	}

	private String asJson(Object value) {
		if (value instanceof SignupRequest request) {
			return """
					{
					  "email": "%s",
					  "password": "%s",
					  "nickname": "%s"
					}
					""".formatted(request.email(), request.password(), request.nickname());
		}
		if (value instanceof LoginRequest request) {
			return """
					{
					  "email": "%s",
					  "password": "%s"
					}
					""".formatted(request.email(), request.password());
		}
		throw new IllegalArgumentException("지원하지 않는 요청 타입입니다.");
	}

	private long insertActiveEvent(String eventName, int winnerLimit) {
		LocalDateTime now = LocalDateTime.now().withNano(0);
		return insertEvent(eventName, "기본 상품", now.minusMinutes(1), now.plusMinutes(10), winnerLimit);
	}

	private long insertEvent(
			String eventName,
			String productName,
			LocalDateTime startAt,
			LocalDateTime endAt,
			int winnerLimit
	) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		LocalDateTime now = LocalDateTime.now().withNano(0);
		String status = now.isBefore(startAt) ? "SCHEDULED" : (now.isBefore(endAt) ? "ACTIVE" : "ENDED");

		jdbcTemplate.update(connection -> {
			PreparedStatement statement = connection.prepareStatement("""
					INSERT INTO events (
					    event_name,
					    product_name,
					    representative_attach_id,
					    start_at,
					    end_at,
					    winner_limit,
					    winner_count,
					    participant_count,
					    status,
					    created_by,
					    created_at,
					    updated_at
					) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
					""", Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, eventName);
			statement.setString(2, productName);
			statement.setObject(3, null);
			statement.setObject(4, startAt);
			statement.setObject(5, endAt);
			statement.setInt(6, winnerLimit);
			statement.setInt(7, 0);
			statement.setInt(8, 0);
			statement.setString(9, status);
			statement.setObject(10, null);
			statement.setObject(11, now);
			statement.setObject(12, now);
			return statement;
		}, keyHolder);

		return keyHolder.getKey().longValue();
	}

	private long insertAttach(
			String attachType,
			String originalFileName,
			String storedFileName,
			Path filePath,
			String contentType
	) throws Exception {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		long fileSize = Files.size(filePath);
		jdbcTemplate.update(connection -> {
			PreparedStatement statement = connection.prepareStatement("""
					INSERT INTO attach (
					    attach_type,
					    original_file_name,
					    stored_file_name,
					    file_path,
					    content_type,
					    file_size,
					    created_at
					) VALUES (?, ?, ?, ?, ?, ?, ?)
					""", Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, attachType);
			statement.setString(2, originalFileName);
			statement.setString(3, storedFileName);
			statement.setString(4, filePath.toString());
			statement.setString(5, contentType);
			statement.setLong(6, fileSize);
			statement.setObject(7, LocalDateTime.now().withNano(0));
			return statement;
		}, keyHolder);
		return keyHolder.getKey().longValue();
	}

}
