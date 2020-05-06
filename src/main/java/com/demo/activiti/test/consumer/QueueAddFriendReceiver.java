//package com.demo.activiti.test.consumer;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.jms.annotation.JmsListener;
//import org.springframework.stereotype.Component;
//
//
//@Component
//@Slf4j
//public class QueueAddFriendReceiver {
//
//    @JmsListener(destination = "QUEUE_RECEIVE_ADD_FIREND", containerFactory = "MyjmsQueueListener") //QUEUE_RECEIVE_ADD_FIREND为监听的队列名称
//    public void receiveAddFriend(String student) {
//        System.out.println("我接收到的消息"+student);
//        log.error("receiveAddFriend Exception:{}");
//    }
//}