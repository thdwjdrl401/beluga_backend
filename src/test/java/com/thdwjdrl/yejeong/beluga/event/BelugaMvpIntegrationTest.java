package com.thdwjdrl.yejeong.beluga.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import com.thdwjdrl.yejeong.beluga.participation.ParticipantResponse;
import com.thdwjdrl.yejeong.beluga.participation.ParticipationResponse;
import com.thdwjdrl.yejeong.beluga.participation.ParticipationResultStatus;
import com.thdwjdrl.yejeong.beluga.participation.ParticipationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BelugaMvpIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private EventService eventService;

	@Autowired
	private ParticipationService participationService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void setUp() {
		jdbcTemplate.update("DELETE FROM event_participations");
		jdbcTemplate.update("DELETE FROM users");
		jdbcTemplate.update("DELETE FROM events");
	}

	@Test
	void createParticipateAndQueryFlowWorks() throws Exception {
		LocalDateTime now = LocalDateTime.now().withNano(0);
		CreateEventRequest request = new CreateEventRequest(
				"벨루가 오픈 이벤트",
				"테스트용 이벤트",
				now.minusMinutes(1),
				now.plusMinutes(10),
				2
		);

		String createResponse = mockMvc.perform(post("/admin/events")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "벨루가 오픈 이벤트",
								  "description": "테스트용 이벤트",
								  "startAt": "%s",
								  "endAt": "%s",
								  "winnerLimit": 2
								}
								""".formatted(request.startAt(), request.endAt())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andReturn()
				.getResponse()
				.getContentAsString();

		Map<String, Object> createdEvent = JsonParserFactory.getJsonParser().parseMap(createResponse);
		long eventId = ((Number) createdEvent.get("eventId")).longValue();

		mockMvc.perform(post("/events/{eventId}/participate", eventId)
						.header("X-User-Email", "winner@example.com"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultStatus").value("WIN"))
				.andExpect(jsonPath("$.requestSequence").value(1L));

		mockMvc.perform(get("/events/{eventId}/result", eventId)
						.header("X-User-Email", "winner@example.com"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.participated").value(true))
				.andExpect(jsonPath("$.resultStatus").value("WIN"));

		mockMvc.perform(get("/admin/events/{eventId}/winners", eventId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].userEmail").value("winner@example.com"))
				.andExpect(jsonPath("$[0].requestSequence").value(1L));
	}

	@Test
	void duplicateParticipationReturnsDuplicateWithoutAdditionalHistory() throws Exception {
		EventResponse event = createActiveEvent(1);

		mockMvc.perform(post("/events/{eventId}/participate", event.eventId())
						.header("X-User-Email", "duplicate@example.com"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultStatus").value("WIN"));

		mockMvc.perform(post("/events/{eventId}/participate", event.eventId())
						.header("X-User-Email", "duplicate@example.com"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultStatus").value("DUPLICATE"))
				.andExpect(jsonPath("$.existingResultStatus").value("WIN"));

		mockMvc.perform(get("/admin/events/{eventId}/participants", event.eventId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));
	}

	@Test
	void participateReturnsBeforeStartAndEndedForInactiveEvents() throws Exception {
		LocalDateTime now = LocalDateTime.now().withNano(0);
		EventResponse scheduledEvent = eventService.createEvent(new CreateEventRequest(
				"예정 이벤트",
				"아직 시작 전",
				now.plusMinutes(1),
				now.plusMinutes(30),
				1
		));
		EventResponse endedEvent = eventService.createEvent(new CreateEventRequest(
				"종료 이벤트",
				"이미 종료",
				now.minusMinutes(30),
				now.minusMinutes(1),
				1
		));

		mockMvc.perform(post("/events/{eventId}/participate", scheduledEvent.eventId())
						.header("X-User-Email", "user@example.com"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultStatus").value("BEFORE_START"));

		mockMvc.perform(post("/events/{eventId}/participate", endedEvent.eventId())
						.header("X-User-Email", "user@example.com"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultStatus").value("ENDED"));
	}

	@Test
	void missingStaticResourceReturnsNotFoundInsteadOfSystemError() throws Exception {
		mockMvc.perform(get("/favicon.ico"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("NOT_FOUND"))
				.andExpect(jsonPath("$.message").value("요청한 리소스를 찾을 수 없습니다."));
	}

	@Test
	void concurrentParticipationKeepsWinnerLimitAndSequenceOrder() throws Exception {
		EventResponse event = createActiveEvent(5);
		ExecutorService executorService = Executors.newFixedThreadPool(20);
		CountDownLatch readyLatch = new CountDownLatch(20);
		CountDownLatch startLatch = new CountDownLatch(1);

		try {
			List<Future<ParticipationResponse>> futures = IntStream.range(0, 20)
					.mapToObj(index -> executorService.submit(() -> {
						readyLatch.countDown();
						startLatch.await(5, TimeUnit.SECONDS);
						return participationService.participate(event.eventId(), "user" + index + "@example.com");
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

			assertThat(responses).hasSize(20);
			assertThat(responses.stream()
					.filter(response -> response.resultStatus() == ParticipationResultStatus.WIN)
					.count()).isEqualTo(5);
			assertThat(responses.stream()
					.filter(response -> response.resultStatus() == ParticipationResultStatus.LOSE)
					.count()).isEqualTo(15);

			List<ParticipantResponse> participants = participationService.getParticipants(event.eventId());
			List<ParticipantResponse> winners = participationService.getWinners(event.eventId());

			assertThat(participants).hasSize(20);
			assertThat(winners).hasSize(5);
			assertThat(participants.stream().map(ParticipantResponse::requestSequence).toList())
					.containsExactlyElementsOf(LongStream.rangeClosed(1, 20).boxed().toList());
			assertThat(winners.stream().map(ParticipantResponse::requestSequence).toList())
					.containsExactlyElementsOf(LongStream.rangeClosed(1, 5).boxed().toList());
		}
		finally {
			executorService.shutdownNow();
		}
	}

	private EventResponse createActiveEvent(int winnerLimit) {
		LocalDateTime now = LocalDateTime.now().withNano(0);
		return eventService.createEvent(new CreateEventRequest(
				"활성 이벤트",
				"동시성 테스트용",
				now.minusMinutes(1),
				now.plusMinutes(10),
				winnerLimit
		));
	}

}
