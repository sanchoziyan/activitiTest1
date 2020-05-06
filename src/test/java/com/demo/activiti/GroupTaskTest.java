package com.demo.activiti;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * 组任务
 */
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class GroupTaskTest {
    private Logger logger = LoggerFactory.getLogger(GroupTaskTest.class);

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
                .name("组任务") //添加部署的名称
                .addClasspathResource("workflow/groupTask.bpmn")//从classpath加载资源文件 一次只能加载一个文件
//                .addClasspathResource("workflow/exclusiveGaeWay.png")
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
        String processDefinitionKey = "groupTask";
        /** 启动流程实例的同时，设置流程变量**/
//        Map<String,Object> variables = new HashMap<String,Object>();//userId-->流程变量
//        variables.put("userId","周芷若");
        //返回流程实例对象
        ProcessInstance processInstance = processEngine.getRuntimeService() //正在执行的流程实例和执行对象相关的对象--执行管理 ，包括启动 推进 删除流程实例等操作
                .startProcessInstanceByKey(processDefinitionKey);//使用流程定义的key 启动实例，key 为bpmn 文件id 的属性值myProcess  或者 流程定义表：act_re_procdef 表中的key
        //默认使用的最新版本的流程定义启动
        logger.info("流程实例id:"+processInstance.getId());//流程实例id:2501
        logger.info("流程定义id:"+processInstance.getProcessDefinitionId());//流程定义id:myProcess:1:4
        //act_ru_execution  act_ru_task  act_ru_identitylink  act_ru_execution  act_hi_taskinst  act_hi_procinst  act_hi_identitylink act_hi_actinst
    }

    /**
     * 查询当前人的个人任务/ 组任务和办理人查询
     */
    @Test
    public void findMyPersonProcessTask(){
//        String assignee = "张翠山";//张三丰  周芷若 张翠山
        String candidateUser = "小A";
        //任务管理
        List<Task> list = processEngine.getTaskService() //与任务相关的service
                .createTaskQuery()
//                .taskAssignee(assignee) //指定个人任务查询 指定办理人  act_ru_task
                .taskCandidateUser(candidateUser)//组任务和办理人查询
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

    /** 查询正在执行的办理人表**/
    @Test
    public void findRunPeronsTask(){
        String taskId = "";
        List<IdentityLink> list = processEngine.getTaskService()
                .getIdentityLinksForTask(taskId);
        if(list!=null && list.size()>0){
            for(IdentityLink d:list){
                System.out.println(d.getTaskId()+" "+d.getType()+" "+d.getProcessInstanceId()+" "+d.getUserId());
            }
        }

    }

    /** 查询历史任务的办理人表**/
    @Test
    public void findHisPeronsTask(){
        //流程实例ID
        String processInstanceId ="";
        List<HistoricIdentityLink> list = processEngine.getHistoryService()
                .getHistoricIdentityLinksForProcessInstance(processInstanceId);
        if(list!=null && list.size()>0){
            for(HistoricIdentityLink h:list){
                System.out.println(h.getTaskId()+" "+h.getType()+ " "+h.getUserId());
            }
        }

    }

    /** 拾取任务，将组任务分给个人任务，指定任务的办理人字段**/
    @Test
    public void claim(){
        //将组任务分配给个人任务
        //任务id
        String taskId = "165005";//165005
        //分配个人任务，可以是组任务中的成员，也可以是非组任务的成员
        String userId ="大F";
        processEngine.getTaskService()
                .claim(taskId,userId);

    }

    /** 将个人任务回退到组任务,前提之前一定是组任务**/
    @Test
    public void setAssigee(){
//任务id
        String taskId = "165005";//165005
        processEngine.getTaskService()
                .setAssignee(taskId,null);
    }

    /** 向组任务中添加成员**/
    @Test
    public void addGroupUser(){
        //任务ID
        String taskId = "165005";
        //成员ID
        String userId = "大H";
        processEngine.getTaskService()
                .addCandidateUser(taskId,userId);
    }

    /** 从组任务中删除成员**/
    @Test
    public void deleteGroupUser(){
//任务ID
        String taskId = "165005";
        //成员ID
        String userId = "大H";
        processEngine.getTaskService()
                .deleteCandidateUser(taskId,userId);
    }

    /**
     * 完成我的任务
     */
    @Test
    public void completeMyPersonTask(){
        //任务id
        String taskId = "147505"; //72505  75005   82505  85005 127507  142505 147505
        //完成任务的同时 设置流程变量  使用流程变量用来指定完成任务后 下一个连线，对应sequenceFlow.bpmn 文件中的 #{message=='不重要'}
//        Map<String,Object> variables = new HashMap<String,Object>();
//        variables.put("hahaha","哈哈哈");
        processEngine.getTaskService() //与任务相关的service
                .complete(taskId);
//                .complete(taskId,variables); //完成任务
        logger.info("完成任务，任务id:"+taskId); //完成任务，任务id:5002

    }

    /**
     * 可以分配个人任务从一个人到另一个人（认领任务）
     */
    @Test
    public void setAssigneeTask(){
        /** 任务ID  和指定办理人ID**/
        String taskId ="147505";
        String userId ="张翠山";
        processEngine.getTaskService()
                .setAssignee(taskId,userId);
    }

    /**
     * 删除流程定义
     */
    @Test
    public void deleteaProcessDefinition(){
        String deploymentId = "157501";
        //使用部署ID 完成删除
//        processEngine.getRepositoryService()//
//        .deleteDeployment(deploymentId); //不能级联删除，只能删除没有启动的流程，如果流程启动就会抛异常

        //不管流程是否启动，都能删除
        processEngine.getRepositoryService()//

                .deleteDeployment(deploymentId,true);//默认是false  实现级联删除。
    }
}
