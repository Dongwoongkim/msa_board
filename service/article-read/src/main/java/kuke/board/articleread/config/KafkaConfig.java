package kuke.board.articleread.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
public class KafkaConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
        ConsumerFactory<String, String> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        // 메시지를 받은 후 명시적으로 ack.acknowledge() 호출할 때까지
        // 메시지를 처리중인 것으로 간주 (실패 시 재시도)
        // 메시지를 완전히 처리한 후에만 오프셋을 커밋
        // 처리 중 예외 발생 시 메시지는 나중에 재처리됨
        // 메시지 손실 방지용
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }
}
