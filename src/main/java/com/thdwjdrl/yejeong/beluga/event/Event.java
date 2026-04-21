package com.thdwjdrl.yejeong.beluga.event;

import java.time.LocalDateTime;

public class Event {

	private Long eventId;
	private String title;
	private String description;
	private LocalDateTime startAt;
	private LocalDateTime endAt;
	private int winnerLimit;
	private int currentWinnerCount;
	private long lastRequestSequence;
	private EventStatus status;
	private LocalDateTime createdAt;

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public int getCurrentWinnerCount() {
		return currentWinnerCount;
	}

	public void setCurrentWinnerCount(int currentWinnerCount) {
		this.currentWinnerCount = currentWinnerCount;
	}

	public long getLastRequestSequence() {
		return lastRequestSequence;
	}

	public void setLastRequestSequence(long lastRequestSequence) {
		this.lastRequestSequence = lastRequestSequence;
	}

	public EventStatus getStatus() {
		return status;
	}

	public void setStatus(EventStatus status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

}
