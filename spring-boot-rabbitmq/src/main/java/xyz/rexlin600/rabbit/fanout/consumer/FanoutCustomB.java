package xyz.rexlin600.rabbit.fanout.consumer;

import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import xyz.rexlin600.rabbit.fanout.config.FanoutConfig;

/**
 * Fanout 消费者类B
 *
 * @author: hekunlin
 * @date: 2020/1/7
 */
@Slf4j
@Component
@RabbitListener(queues = FanoutConfig.FANOUT_QUEUE_B)
public class FanoutCustomB {

    /**
     * 处理消息 String
     *
     * @param message
     */
    @SneakyThrows
    @RabbitHandler
    public void handlerFanOutStrB(String content, Channel channel, Message message) {
        log.info("==>  Fanout B consume message=[{}] and content=[{}]", message, content);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);   // true表示一次确认所有小于tag的消息
    }

}