package com.thdwjdrl.yejeong.beluga.event;

import java.time.LocalDateTime;

public class Event {

	private Long eventId;
	private String eventName;
	private String productName;
	private Long representativeAttachId;
	private LocalDateTime startAt;
	private LocalDateTime endAt;
	private int winnerLimit;
	private int winnerCount;
	private int participantCount;
	private EventStatus status;
	private Long createdBy;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

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

	public Long getRepresentativeAttachId() {
		return representativeAttachId;
	}

	public void setRepresentativeAttachId(Long representativeAttachId) {
		this.representativeAttachId = representativeAttachId;
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

	public int getWinnerLimit() {
		return winnerLimit;
	}

	public void setWinnerLimit(int winnerLimit) {
		this.winnerLimit = winnerLimit;
	}

	public int getWinnerCount() {
		return winnerCount;
	}

	public void setWinnerCount(int winnerCount) {
		this.winnerCount = winnerCount;
	}

	public int getParticipantCount() {
		return participantCount;
	}

	public void setParticipantCount(int participantCount) {
		this.participantCount = participantCount;
	}

	public EventStatus getStatus() {
		return status;
	}

	public void setStatus(EventStatus status) {
		this.status = status;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

}
