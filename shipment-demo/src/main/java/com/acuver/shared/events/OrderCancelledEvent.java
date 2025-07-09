package com.acuver.shared.events;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
public class OrderCancelledEvent extends BaseEvent {
    private String customerId;
    private String reason;
    private String status;
}