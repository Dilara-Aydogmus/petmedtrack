package org.example.notification;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class EmailProducer {
    private final RabbitTemplate rabbitTemplate;

    public EmailProducer(RabbitTemplate rabbitTemplate){
        this.rabbitTemplate = rabbitTemplate;
    }
    public void sendEmail(EmailMessage emailMessage){
        rabbitTemplate.convertAndSend(RabbitMqConfig.EMAIL_QUEUE, emailMessage);
    }
}
