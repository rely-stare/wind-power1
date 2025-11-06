package com.tc.common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMqConfig {

    // ---------------------------------转速---------------------------------
    public static final String SPEED_DATA_QUEUE = "speed_data_queue";
    public static final String SPEED_DATA_EXCHANGE = "speed_data_exchange";
    public static final String SPEED_ROUTING_KEY = "speed_data";

    // ---------------------------------温度---------------------------------
    public static final String TEMPERATURE_DATA_QUEUE = "temperature_data_queue";
    public static final String TEMPERATURE_DATA_EXCHANGE = "temperature_data_exchange";
    public static final String TEMPERATURE_ROUTING_KEY = "temperature_data";

    // 实时交换机和队列
    @Bean
    public DirectExchange speedExchange() {
        return new DirectExchange(SPEED_DATA_EXCHANGE, true, false);
    }

    @Bean
    public Queue speedQueue() {
        return new Queue(SPEED_DATA_QUEUE, true);
    }

    @Bean
    public Binding realTimeBinding() {
        return BindingBuilder.bind(speedQueue()).to(speedExchange()).with(SPEED_ROUTING_KEY);
    }

    @Bean
    public DirectExchange temperatureExchange() {
        return new DirectExchange(TEMPERATURE_DATA_EXCHANGE, true, false);
    }

    @Bean
    public Queue temperatureQueue() {
        return new Queue(TEMPERATURE_DATA_QUEUE, true);
    }

    @Bean
    public Binding temperatureBinding() {
        return BindingBuilder.bind(temperatureQueue()).to(temperatureExchange()).with(TEMPERATURE_ROUTING_KEY);
    }

}

