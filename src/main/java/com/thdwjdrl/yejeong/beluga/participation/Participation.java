package com.thdwjdrl.yejeong.beluga.participation;

import java.time.LocalDateTime;

public class Participation {

	private Long participationId;
	private Long eventId;
	private Long userId;
	private LocalDateTime participatedAt;
	private long requestSequence;
	private ParticipationResultStatus resultStatus;
	private Long gifticonAttachId;
	private LocalDateTime createdAt;

	public Long getParticipationId() {
		return participationId;
	}

	public void setParticipationId(Long participationId) {
		this.participationId = participationId;
	}

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public LocalDateTime getParticipatedAt() {
		return participatedAt;
	}

	public void setParticipatedAt(LocalDateTime participatedAt) {
		this.participatedAt = participatedAt;
	}

	public long getRequestSequence() {
		return requestSequence;
	}

	public void setRequestSequence(long requestSequence) {
		this.requestSequence = requestSequence;
	}

	public ParticipationResultStatus getResultStatus() {
		return resultStatus;
	}

	public void setResultStatus(ParticipationResultStatus resultStatus) {
		this.resultStatus = resultStatus;
	}

	public Long getGifticonAttachId() {
		return gifticonAttachId;
	}

	public void setGifticonAttachId(Long gifticonAttachId) {
		this.gifticonAttachId = gifticonAttachId;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

}
