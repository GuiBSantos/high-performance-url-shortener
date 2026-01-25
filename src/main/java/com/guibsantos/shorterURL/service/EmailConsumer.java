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

@Component
@RequiredArgsConstructor
public class EmailConsumer {

    private final JavaMailSender mailSender;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void listen(@Payload EmailDto emailDto) {
        try {
            System.out.println("📨 [Consumer] Processando e-mail para: " + emailDto.to());

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(emailDto.to());
            helper.setSubject(emailDto.subject());

            helper.setText(emailDto.body(), true);

            helper.setFrom("app.shortener@gmail.com");

            mailSender.send(message);

            System.out.println(" [Consumer] E-mail enviado com sucesso!");

        } catch (MessagingException e) {
            System.err.println(" [Consumer] Erro ao enviar: " + e.getMessage());
        }
    }
}