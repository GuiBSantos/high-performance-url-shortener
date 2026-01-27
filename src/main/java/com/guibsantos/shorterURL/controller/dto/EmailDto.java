package com.guibsantos.shorterURL.controller.dto;

import java.io.Serializable;

public record EmailDto(
        String to,
        String subject,
        String body,
        String emailType
) implements Serializable {}
