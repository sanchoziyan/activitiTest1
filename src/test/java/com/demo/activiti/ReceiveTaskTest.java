package com.demo.activiti;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.Execution;
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
 * 接收任务活动 即等待活动
 */
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class ReceiveTaskTest {
    private Logger logger = LoggerFactory.getLogger(ReceiveTaskTest.class);

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
                .name("接收活动") //添加部署的名称
                .addClasspathResource("workflow/receiveTask.bpmn")//从classpath加载资源文件 一次只能加载一个文件
//                .addClasspathResource("workflow/sequenceFlow.png")
                .deploy();//完成部署
        logger.info("id:" + deploy.getId());//1
        logger.info("id:" + deploy.getName());//对应的表act_re_procdef（） act_re_deployment（部署表） act_ge_bytearray （二进制表） act_ge_property（版本号控制的）
    }

    /**
     * 启动流程实例，设置流程变量 获取流程变量 向后执行一步
     */
    @Test
    public void startProcessInstance() {
        //流程顶定义的key
        String processDefinitionKey = "receiveTask";
        //返回流程实例对象
        ProcessInstance processInstance = processEngine.getRuntimeService() //正在执行的流程实例和执行对象相关的对象--执行管理 ，包括启动 推进 删除流程实例等操作
                .startProcessInstanceByKey(processDefinitionKey);//使用流程定义的key 启动实例，key 为bpmn 文件id 的属性值myProcess  或者 流程定义表：act_re_procdef 表中的key
        //默认使用的最新版本的流程定义启动
        logger.info("流程实例id:" + processInstance.getId());//流程实例id:2501
        logger.info("流程定义id:" + processInstance.getProcessDefinitionId());//流程定义id:myProcess:1:4
        //act_ru_execution  act_ru_task  act_ru_identitylink  act_ru_execution  act_hi_taskinst  act_hi_procinst  act_hi_identitylink act_hi_actinst
        /** 查询执行对象ID**/
        String processInstanceId = processInstance.getId();
        String activityId = "_4";
        Execution execution = processEngine.getRuntimeService()
                .createExecutionQuery()//创建执行对象查询
                .processInstanceId(processInstanceId)
                .activityId(activityId)//当前活动Id,对应bpmn文件中的活动节点Id的值（receiveTask-->id）
                .singleResult();
/**使用流程变量设置当日销售额 用来传递业务参数*/
        String executionId = execution.getId();
        processEngine.getRuntimeService()
                .setVariable(executionId, "汇总当日销售额", 21000);

        /** 向后执行一步，如果流程处于等待状态，使得流程继续执行**/
        processEngine.getRuntimeService()
                .signalEventReceived(executionId);

        /** 流程变量中获取汇总当日销售额的值 **/
        String variableName = "汇总当日销售额";
        Integer value = (Integer) processEngine.getRuntimeService()
                .getVariable(executionId, variableName);

        System.out.println("给老板发送短信：金额是："+value);

        /**查询执行对象ID*/
        Execution execution2 = processEngine.getRuntimeService()
                .createExecutionQuery()//创建执行对象查询
                .processInstanceId(processInstanceId)//使用流程实例ID查询
                .activityId("_5")//当前活动的id，对应receiveTask.bpmn文件中的活动节点的id的属性值
                .singleResult();
        /** 向后执行一步，如果流程处于等待状态，使得流程继续执行**/
        processEngine.getRuntimeService()
                .signalEventReceived(execution2.getId());
    }

    /**
     * 查询当前人的个人任务
     */
    @Test
    public void findMyPersonProcessTask() {
        String assignee = "赵六";//张三 李四 赵六
        //任务管理
        List<Task> list = processEngine.getTaskService() //与任务相关的service
                .createTaskQuery()
                .taskAssignee(assignee) //指定个人任务查询 指定办理人  act_ru_task
                .list();
        if (list != null && list.size() > 0) {
            for (Task task : list) {
                logger.info("任务id:" + task.getId());//任务id:2505  任务id:5002
                logger.info("任务名称:" + task.getName());//任务名称:提交申请  任务名称:部门审批
                logger.info("任务的创建时间:" + task.getCreateTime());//任务的创建时间:Tue Apr 14 18:08:52 CST 2020  任务的创建时间:Tue Apr 14 18:42:29 CST 20
                logger.info("任务的办理人:" + task.getAssignee());//任务的办理人:张三 任务的办理人:李四
                logger.info("流程实例Id:" + task.getProcessInstanceId());//流程实例Id:2501  流程实例Id:2501
                logger.info("执行对象Id:" + task.getExecutionId());//执行对象Id:2502 执行对象Id:2502
                logger.info("流程定义Id:" + task.getProcessDefinitionId());//流程定义Id:myProcess:1:4 流程定义Id:myProcess:1:4
                logger.info("***********************************************");
            }
        }
    }

    /**
     * 完成我的任务
     */
    @Test
    public void completeMyPersonTask() {
        //任务id
        String taskId = "60004"; //50005  57505  60004
        //完成任务的同时 设置流程变量  使用流程变量用来指定完成任务后 下一个连线，对应sequenceFlow.bpmn 文件中的 #{message=='不重要'}
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("message", "重要");
        processEngine.getTaskService() //与任务相关的service
                .complete(taskId, variables); //完成任务
        logger.info("完成任务，任务id:" + taskId); //完成任务，任务id:5002

    }
}
