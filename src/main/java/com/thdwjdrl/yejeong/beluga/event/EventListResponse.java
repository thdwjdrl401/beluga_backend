package com.thdwjdrl.yejeong.beluga.event;

import java.util.List;

public record EventListResponse(
		List<EventSummaryResponse> activeEvents,
		List<EventSummaryResponse> scheduledEvents
) {
}
