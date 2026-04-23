package com.thdwjdrl.yejeong.beluga.participation;

import java.time.LocalDateTime;

public class MyParticipationRow {

	private Long eventId;
	private String eventName;
	private String productName;
	private LocalDateTime participatedAt;
	private ParticipationResultStatus resultStatus;
	private Long gifticonAttachId;
	private LocalDateTime startAt;
	private LocalDateTime endAt;

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public LocalDateTime getParticipatedAt() {
		return participatedAt;
	}

	public void setParticipatedAt(LocalDateTime participatedAt) {
		this.participatedAt = participatedAt;
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

	public LocalDateTime getStartAt() {
		return startAt;
	}

	public void setStartAt(LocalDateTime startAt) {
		this.startAt = startAt;
	}

	public LocalDateTime getEndAt() {
		return endAt;
	}

	public void setEndAt(LocalDateTime endAt) {
		this.endAt = endAt;
	}

}
