package com.demo.activiti;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 排他网关
 */
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class ExclusiveGaeWayTest {
    private Logger logger = LoggerFactory.getLogger(ExclusiveGaeWayTest.class);

    /**
     * 默认方式获取  流程引擎对象
     */
    @Autowired
    ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

    /**
     * 部署流程定义
     */
    @Test
    public void deployment() {
        //与流程定义和流程部署相关的service
        Deployment deploy = processEngine.getRepositoryService()
                .createDeployment() //创建一个部署对象
                .name("排他网关") //添加部署的名称
                .addClasspathResource("workflow/exclusiveGaeWay.bpmn")//从classpath加载资源文件 一次只能加载一个文件
                .addClasspathResource("workflow/exclusiveGaeWay.png")
                .deploy();//完成部署
        logger.info("id:" + deploy.getId());//1
        logger.info("id:" + deploy.getName());//对应的表act_re_procdef（） act_re_deployment（部署表） act_ge_bytearray （二进制表） act_ge_property（版本号控制的）
    }

    /**
     * 启动流程实例
     */
    @Test
    public void startProcessInstance() {
        //流程顶定义的key
        String processDefinitionKey = "exclusiveGaeWay";
        //返回流程实例对象
        ProcessInstance processInstance = processEngine.getRuntimeService() //正在执行的流程实例和执行对象相关的对象--执行管理 ，包括启动 推进 删除流程实例等操作
                .startProcessInstanceByKey(processDefinitionKey);//使用流程定义的key 启动实例，key 为bpmn 文件id 的属性值myProcess  或者 流程定义表：act_re_procdef 表中的key
        //默认使用的最新版本的流程定义启动
        logger.info("流程实例id:"+processInstance.getId());//流程实例id:2501
        logger.info("流程定义id:"+processInstance.getProcessDefinitionId());//流程定义id:myProcess:1:4
        //act_ru_execution  act_ru_task  act_ru_identitylink  act_ru_execution  act_hi_taskinst  act_hi_procinst  act_hi_identitylink act_hi_actinst
    }

    /**
     * 查询当前人的个人任务
     */
    @Test
    public void findMyPersonProcessTask(){
        String assignee = "赵小六";//王小五 胡小八  赵小六
        //任务管理
        List<Task> list = processEngine.getTaskService() //与任务相关的service
                .createTaskQuery()
                .taskAssignee(assignee) //指定个人任务查询 指定办理人  act_ru_task
                .list();
        if(list!= null && list.size()>0){
            for(Task task:list){
                logger.info("任务id:"+task.getId());//任务id:2505  任务id:5002
                logger.info("任务名称:"+task.getName());//任务名称:提交申请  任务名称:部门审批
                logger.info("任务的创建时间:"+task.getCreateTime());//任务的创建时间:Tue Apr 14 18:08:52 CST 2020  任务的创建时间:Tue Apr 14 18:42:29 CST 20
                logger.info("任务的办理人:"+task.getAssignee());//任务的办理人:张三 任务的办理人:李四
                logger.info("流程实例Id:"+task.getProcessInstanceId());//流程实例Id:2501  流程实例Id:2501
                logger.info("执行对象Id:"+task.getExecutionId());//执行对象Id:2502 执行对象Id:2502
                logger.info("流程定义Id:"+task.getProcessDefinitionId());//流程定义Id:myProcess:1:4 流程定义Id:myProcess:1:4
                logger.info("***********************************************");
            }
        }
    }

    /**
     * 完成我的任务
     */
    @Test
    public void completeMyPersonTask(){
        //任务id
        String taskId = "85005"; //72505  75005   82505  85005
        //完成任务的同时 设置流程变量  使用流程变量用来指定完成任务后 下一个连线，对应sequenceFlow.bpmn 文件中的 #{message=='不重要'}
        Map<String,Object> variables = new HashMap<String,Object>();
        variables.put("money",800);
        processEngine.getTaskService() //与任务相关的service
                .complete(taskId,variables); //完成任务
        logger.info("完成任务，任务id:"+taskId); //完成任务，任务id:5002

    }

}
