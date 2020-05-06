package com.demo.activiti.test;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {
    private Logger logger = LoggerFactory.getLogger(TestController.class);

    /**
     * 获取默认流程引擎对象
     */
    @Autowired
    ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

    /**
     * 部署流程定义
     */
    @GetMapping("/test")
    public void deployment(){
        //与流程定义和流程部署相关的service
        Deployment deploy = processEngine.getRepositoryService()
                .createDeployment() //创建一个部署对象
                .name("审批流") //添加部署的名称
                .addClasspathResource("workflow/testActiviti.bpmn")//从classpath加载资源文件 一次只能加载一个文件
                .addClasspathResource("workflow/testActiviti.png")
                .deploy();//完成部署
        logger.info("id:"+deploy.getId());
        logger.info("id:"+deploy.getName());
    }

}
