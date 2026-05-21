package com.thdwjdrl.yejeong.beluga.user;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.thdwjdrl.yejeong.beluga.attach.Attach;
import com.thdwjdrl.yejeong.beluga.attach.AttachService;
import com.thdwjdrl.yejeong.beluga.common.api.SuccessResponse;
import com.thdwjdrl.yejeong.beluga.participation.MyParticipationResponse;
import com.thdwjdrl.yejeong.beluga.participation.ParticipationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me")
public class MeController {

	private final SessionUserService sessionUserService;
	private final UserService userService;
	private final ParticipationService participationService;
	private final AttachService attachService;

	public MeController(
			SessionUserService sessionUserService,
			UserService userService,
			ParticipationService participationService,
			AttachService attachService
	) {
		this.sessionUserService = sessionUserService;
		this.userService = userService;
		this.participationService = participationService;
		this.attachService = attachService;
	}

	@GetMapping
	public SuccessResponse<UserProfileResponse> getMe(HttpSession session) {
		return SuccessResponse.success(userService.toProfile(sessionUserService.requireCurrentUser(session)));
	}

	@PatchMapping("/nickname")
	public SuccessResponse<UserProfileResponse> updateNickname(@RequestBody UpdateNicknameRequest request, HttpSession session) {
		User currentUser = sessionUserService.requireCurrentUser(session);
		return SuccessResponse.success(
				"닉네임이 변경되었습니다.",
				userService.toProfile(userService.updateNickname(currentUser.getUserId(), request))
		);
	}

	@PatchMapping("/password")
	public SuccessResponse<Void> updatePassword(@RequestBody UpdatePasswordRequest request, HttpSession session) {
		User currentUser = sessionUserService.requireCurrentUser(session);
		userService.updatePassword(currentUser.getUserId(), request);
		return SuccessResponse.success("비밀번호가 변경되었습니다.", null);
	}

	@GetMapping("/participations")
	public SuccessResponse<List<MyParticipationResponse>> getParticipations(HttpSession session) {
		User currentUser = sessionUserService.requireCurrentUser(session);
		return SuccessResponse.success(participationService.getMyParticipations(currentUser.getUserId()));
	}

	@GetMapping("/participations/{eventId}/gifticon")
	public ResponseEntity<Resource> getGifticon(@PathVariable Long eventId, HttpSession session) {
		User currentUser = sessionUserService.requireCurrentUser(session);
		return buildAttachResponse(
				participationService.getGifticonAttachId(currentUser.getUserId(), eventId),
				false
		);
	}

	@GetMapping("/participations/{eventId}/gifticon/download")
	public ResponseEntity<Resource> downloadGifticon(@PathVariable Long eventId, HttpSession session) {
		User currentUser = sessionUserService.requireCurrentUser(session);
		return buildAttachResponse(
				participationService.getGifticonAttachId(currentUser.getUserId(), eventId),
				true
		);
	}

	private ResponseEntity<Resource> buildAttachResponse(Long attachId, boolean download) {
		Attach attach = attachService.getRequiredAttach(attachId);
		Resource resource = attachService.loadRequiredResource(attach);
		MediaType mediaType = attachService.resolveMediaType(attach);
		ContentDisposition disposition = download
				? ContentDisposition.attachment()
						.filename(attachService.resolveDownloadFileName(attach), StandardCharsets.UTF_8)
						.build()
				: ContentDisposition.inline()
						.filename(attachService.resolveDownloadFileName(attach), StandardCharsets.UTF_8)
						.build();

		return ResponseEntity.ok()
				.contentType(mediaType)
				.header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
				.contentLength(attach.getFileSize())
				.body(resource);
	}

}
