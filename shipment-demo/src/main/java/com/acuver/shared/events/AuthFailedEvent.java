package com.acuver.shared.events;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
public class AuthFailedEvent extends BaseEvent {
    private String customerId;
    private String failureReason;
    private String errorCode;

}