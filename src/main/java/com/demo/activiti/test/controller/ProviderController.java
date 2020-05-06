//package com.demo.activiti.test.controller;
//
//
//import com.demo.activiti.test.produce.QueueProducer;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//
//@RestController
//public class ProviderController {
//    @Autowired
//    private QueueProducer queueProducer;
//    //注入存放消息的队列，用于下列方法一
//
//    @GetMapping("/value")
//    public String value() {
//        String queueName="QUEUE_RECEIVE_ADD_FIREND"; //自定义队列名称
//        String student = "我发布了一条消息";
//        queueProducer.sendObjectMessage(queueName, student);   //发送到MQS
//        return "消息已经发送";
//    }
//
//}
