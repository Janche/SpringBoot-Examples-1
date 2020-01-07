package xyz.rexlin600.rabbit.dl.consumer;

import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import xyz.rexlin600.rabbit.direct.config.DirectConfig;
import xyz.rexlin600.rabbit.dl.config.DeadLetterConfig;

import java.time.Instant;

/**
 * DeadLetter 消费者类
 * <p>
 * 监听队列
 *
 * @author: hekunlin
 * @date: 2020/1/7
 */
@Slf4j
@Component
@RabbitListener(queues = DeadLetterConfig.REDIRECT_QUEUE)
public class DeadLetterExpireConsumer {

    /**
     * 处理消息 String
     *
     * @param message
     */
    @SneakyThrows
    @RabbitHandler
    public void handlerDLExpireStr(String content, Channel channel, Message message) {
        long milli = Instant.now().toEpochMilli();
        log.info("==>  DeadLetter consume expire content=[{}] at [{}]", content, milli);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);   // true表示一次确认所有小于tag的消息
    }

}