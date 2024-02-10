package com.example.notifications.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    private String userId;
    private Channel channel;
    private String subject;
    private String body;
    private Status status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public enum Channel {
        EMAIL,
        SMS
    }

    public enum Status {
        PENDING,
        SENT,
        FAILED
    }
}
