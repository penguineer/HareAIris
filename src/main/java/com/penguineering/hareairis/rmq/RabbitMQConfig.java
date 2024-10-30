package com.penguineering.hareairis.rmq;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    @Value("${hareairis.rabbitmq.queue-chat-requests}")
    private String queueChatRequests;

    @Bean
    public Queue chatRequestsQueue() {
        return new Queue(queueChatRequests, true);
    }

    @Bean
    public SimpleMessageListenerContainer chatRequestsContainer(ConnectionFactory connectionFactory,
                                                                ChatRequestHandler handler) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueChatRequests);
        container.setMessageListener(handler);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setChannelTransacted(true);
        return container;
    }
}