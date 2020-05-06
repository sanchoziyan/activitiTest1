//package com.demo.activimq;
//
//import com.demo.activiti.test.produce.QueueProducer;
//import org.junit.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//@SpringBootTest
//public class Testactivimq {
//    @Autowired
//    private QueueProducer queueProducer;
//    @Test
//    public void test(){
//        String queueName="QUEUE_RECEIVE_ADD_FIREND"; //自定义队列名称
//        String student = "ddddddddd";
//        queueProducer.sendObjectMessage(queueName, student);   //发送到MQS
////        return "消息已经发送";
//    }
//}
//
