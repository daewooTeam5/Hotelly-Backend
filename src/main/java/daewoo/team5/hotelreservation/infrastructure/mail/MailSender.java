package daewoo.team5.hotelreservation.infrastructure.mail;


import org.springframework.scheduling.annotation.Async;

public abstract class MailSender {
    public abstract void sendSimpleMail(String to, String subject, String text);

    public abstract void sendHtmlMail(String to, String subject, String htmlContent);

    @Async
    public void sendSimpleMailAsync(String to, String subject, String text) {
        sendSimpleMail(to, subject, text);
    }

    @Async
    public void sendHtmlMailAsync(String to, String subject, String htmlContent) {
        sendHtmlMail(to, subject, htmlContent);
    }
}