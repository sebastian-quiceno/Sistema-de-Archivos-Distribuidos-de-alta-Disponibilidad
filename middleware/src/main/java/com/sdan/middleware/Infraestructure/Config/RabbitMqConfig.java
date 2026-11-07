package com.sdan.middleware.Infraestructure.Config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE_NAME = "dfsha.tasks.exchange";

    public static final String QUEUE_REPLICATION = "dfsha.tasks.re-replication";
    public static final String QUEUE_PURGE = "dfsha.tasks.purge";
    public static final String QUEUE_HEALING = "dfsha.tasks.healing";

    public static final String ROUTING_REPLICATION = "task.replication";
    public static final String ROUTING_PURGE = "task.purge";
    public static final String ROUTING_HEALING = "task.healing";

    @Bean
    public TopicExchange dfshaTasksExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue replicationQueue() {
        return new Queue(QUEUE_REPLICATION, true);
    }

    @Bean
    public Queue purgeQueue() {
        return new Queue(QUEUE_PURGE, true);
    }

    @Bean
    public Queue healingQueue() {
        return new Queue(QUEUE_HEALING, true);
    }

    @Bean
    public Binding replicationBinding(Queue replicationQueue, TopicExchange dfshaTasksExchange) {
        return BindingBuilder.bind(replicationQueue).to(dfshaTasksExchange).with(ROUTING_REPLICATION);
    }

    @Bean
    public Binding purgeBinding(Queue purgeQueue, TopicExchange dfshaTasksExchange) {
        return BindingBuilder.bind(purgeQueue).to(dfshaTasksExchange).with(ROUTING_PURGE);
    }

    @Bean
    public Binding healingBinding(Queue healingQueue, TopicExchange dfshaTasksExchange) {
        return BindingBuilder.bind(healingQueue).to(dfshaTasksExchange).with(ROUTING_HEALING);
    }

    @Bean
    @SuppressWarnings("removal")
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages(
                "com.sdan.middleware.Application.Inputs.Messages",
                "java.util",
                "java.lang"
        );
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }
}

