package com.healthcare.telemedicine.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "healthcare.user-events.enabled", havingValue = "true", matchIfMissing = true)
public class RabbitMqConfig {

    @Bean
    public TopicExchange userEventsExchange(
            @Value("${healthcare.user-events.exchange}") String exchangeName
    ) {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue userEventsQueue(
            @Value("${healthcare.user-events.queue}") String queueName
    ) {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding userEventsBinding(Queue userEventsQueue, TopicExchange userEventsExchange) {
        return BindingBuilder.bind(userEventsQueue).to(userEventsExchange).with("user.*");
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
