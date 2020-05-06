//package com.demo.activiti.test.controller;
//
//import com.demo.activiti.test.service.WebSocketServer;
//import org.springframework.web.bind.annotation.*;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//public class WebSocketController {
//
//    @RequestMapping(value="/pushVideoListToWeb",method= RequestMethod.POST,consumes = "application/json")
//    public @ResponseBody Map<String,Object> pushVideoListToWeb(@RequestBody Map<String,Object> param) {
//        Map<String,Object> result =new HashMap<String,Object>();
//
//        try {
//            WebSocketServer.sendInfo("有新客户呼入");
//            result.put("operationResult", true);
//        }catch (IOException e) {
//            result.put("operationResult", true);
//        }
//        return result;
//    }
//}
