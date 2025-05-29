package com.kce.ump.dto.request;

import lombok.Data;

@Data
public class RejectionEmailRequest {
    private String subject;
    private String body;
}
