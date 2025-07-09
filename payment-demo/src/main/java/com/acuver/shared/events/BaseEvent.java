package com.acuver.shared.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent {
    private String eventType;
    private String orderId;
    private LocalDateTime timestamp;
} 