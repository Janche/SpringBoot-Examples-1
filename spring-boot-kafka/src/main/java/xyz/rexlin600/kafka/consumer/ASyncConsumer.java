package xyz.rexlin600.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import xyz.rexlin600.kafka.constant.KafkaGroupIdConstant;
import xyz.rexlin600.kafka.constant.KafkaTopicConstant;

import java.util.*;

/**
 * 异步消费者
 *
 * @author: hekunlin
 * @date: 2020/7/2
 */
@Slf4j
@Component
public class ASyncConsumer {

    /**
     * @param records
     * @param consumer
     */
    @KafkaListener(topics = KafkaTopicConstant.TOPIC, groupId = KafkaGroupIdConstant.ASYNC_GROUP_ID)
    public void onMessage(List<ConsumerRecord<String, String>> records, Consumer consumer) {
        // map 记录消费的位移
        Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>(records.size());

        // set 记录消费异常的分区
        Set<Integer> failedPartition = new HashSet<>(records.size());

        // 消费
        for (ConsumerRecord<String, String> record : records) {
            try {
                if (failedPartition.contains(record.partition())) {
                    continue;
                }

                // 模拟消费消息 ...
                log.info("==>  线程编号：[{}]，消息内容：[{}]", Thread.currentThread().getId(), records);

                // 记录位移
                offsets.put(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset() + 1));
            } catch (Exception e) {
                log.error("==>  消费消息发生异常：[{}]", e.getMessage());
                failedPartition.add(record.partition());
                consumer.seek(new TopicPartition(record.topic(), record.partition()), record.offset());
            }
        }

        // 手动提交位移
        consumer.commitSync(offsets);
    }


}