package com.penguineering.hareairis.rmq;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;

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
                                                                MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueChatRequests);
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean
    public MessageListenerAdapter chatRequestsListenerAdapter(ChatRequestHandler handler) {
        MessageListenerAdapter adapter = new MessageListenerAdapter(handler, "handleMessage");
        adapter.setMessageConverter(null); // Ensure the whole message is passed
        return adapter;
    }
}