package com.guibsantos.shorterURL.service;

import com.guibsantos.shorterURL.config.RabbitMQConfig;
import com.guibsantos.shorterURL.controller.dto.EmailDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class EmailConsumer {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void listen(@Payload EmailDto emailDto) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            Context context = new Context();
            String templateName;

            if ("WELCOME".equalsIgnoreCase(emailDto.emailType())) {
                templateName = "welcome";
                context.setVariable("username", emailDto.body());
            }
            else if ("RECOVERY".equalsIgnoreCase(emailDto.emailType())) {
                templateName = "recuperacao-senha";

                String rawBody = emailDto.body();
                String username = "Usuário";
                String code = rawBody;

                if (rawBody != null && rawBody.contains(":")) {
                    String[] parts = rawBody.split(":", 2);
                    username = parts[0];
                    code = parts[1];
                }

                context.setVariable("username", username);
                context.setVariable("code", code);
                context.setVariable("type", "RECOVERY");
            }
            else {
                templateName = "email-template";
                context.setVariable("body", emailDto.body());
                context.setVariable("type", "GENERIC");
                context.setVariable("username", "Usuário");
            }

            String htmlContent = templateEngine.process(templateName, context);

            helper.setTo(emailDto.to());
            helper.setSubject(emailDto.subject());
            helper.setText(htmlContent, true);
            helper.setFrom("app.shortener@gmail.com");

            mailSender.send(message);

        } catch (MessagingException e) {
            System.err.println("Erro ao enviar e-mail: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro genérico no consumidor de email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}