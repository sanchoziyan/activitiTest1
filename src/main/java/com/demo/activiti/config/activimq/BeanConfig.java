package com.demo.activiti.config.activimq;

import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.jms.Queue;

@Configuration
public class BeanConfig {
    //定义存放消息
    @Bean
    public Queue queue(){
        return new ActiveMQQueue("ActiveMQQueue");
    }
}