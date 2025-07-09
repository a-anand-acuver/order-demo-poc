package com.acuver.shared.events;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
public class CancelOrderRequestEvent extends BaseEvent {
    private String reason;
} 