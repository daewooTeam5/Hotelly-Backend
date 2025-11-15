package daewoo.team5.hotelreservation.infrastructure.redis;


import daewoo.team5.hotelreservation.domain.place.event.ReservationCancelEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {
    private final ApplicationEventPublisher eventPublisher;

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer,
                                      ApplicationEventPublisher eventPublisher) {
        super(listenerContainer);
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String key = message.toString();
        if(key.startsWith("place:reservation:cancel")){
            Long reservationId = Long.parseLong(key.split(":")[3]);
            eventPublisher.publishEvent(new ReservationCancelEvent(reservationId));

        }
    }
}
