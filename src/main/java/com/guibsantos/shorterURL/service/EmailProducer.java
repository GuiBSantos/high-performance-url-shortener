package com.guibsantos.shorterURL.service;

import com.guibsantos.shorterURL.config.RabbitMQConfig;
import com.guibsantos.shorterURL.controller.dto.EmailDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailProducer {

    private final RabbitTemplate rabbitTemplate;

    public EmailProducer(RabbitTemplate rabbitTemplate, Jackson2JsonMessageConverter messageConverter) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitTemplate.setMessageConverter(messageConverter);
    }

    /**
     * @param to Destinatário
     * @param subject Assunto
     * @param body Conteúdo
     * @param emailType "GENERIC" ou "WELCOME"
     */
    public void sendEmailMessage(String to, String subject, String body, String emailType) {
        EmailDto emailDto = new EmailDto(to, subject, body, emailType);

        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, emailDto);

        log.info(" [Producer] Mensagem enviada para a fila. Destinatário: {}, Tipo: {}", to, emailType);
    }
}