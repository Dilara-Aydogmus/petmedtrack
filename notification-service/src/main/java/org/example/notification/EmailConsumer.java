package org.example.notification;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {
    private final EmailService emailService;
    public EmailConsumer(EmailService emailservice){
        this.emailService = emailservice;
    }

    @RabbitListener(queues = RabbitMqConfig.EMAIL_QUEUE)
    public void consumeEmail(EmailMessage emailMessage){
        emailService.sendSimpleEmail(emailMessage.getTo(), emailMessage.getSubject(), emailMessage.getText());
    }
}
