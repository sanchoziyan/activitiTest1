package com.demo.activiti;

import com.demo.activiti.entity.Person;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@SpringBootTest//(classes = ActivitiApplicationTests.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
public class ActivitiApplicationTests {
    private Logger logger = LoggerFactory.getLogger(ActivitiApplicationTests.class);

    /**
     * 默认方式获取  流程引擎对象
     */
    @Autowired
    ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

    /**
     * 使用代码创建工作流程需要的23张表
     * <p>
     * public static final String DB_SCHEMA_UPDATE_FALSE = "false"; 不能自动创建表 需要表存在
     * public static final String DB_SCHEMA_UPDATE_CREATE_DROP = "create-drop"; 先删除 再创建
     * public static final String DB_SCHEMA_UPDATE_TRUE = "true"; 如果表不存在 自动创建表
     */
    @Test
    public void createTable() {
        ProcessEngineConfiguration config = ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration();
        config.setJdbcDriver("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://localhost:3306/activiti?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8");
        config.setJdbcUsername("root");
        config.setJdbcPassword("root");
        config.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE);
        //工作流的核心对象 processEngine对象
        ProcessEngine processEngine = config.buildProcessEngine();
        logger.info("processEngine:" + processEngine);
    }

    /**
     * 通过配置文件activiti.cfg.xml 获取流程引擎对象
     */
    @Test
    public void createtable_2() {
        ProcessEngine processEngine = ProcessEngineConfiguration
                .createProcessEngineConfigurationFromResource("activiti.cfg.xml")
                .buildProcessEngine();
        logger.info("processEngin:" + processEngine);
    }

    @Test
    public void contextLoads() {
        //通过获取载入默认获取
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        logger.info(processEngine.toString());
        processEngine.close();

    }

    /**
     * 部署流程定义
     */
    @Test
    public void deployment() {
        //与流程定义和流程部署相关的service
        Deployment deploy = processEngine.getRepositoryService()
                .createDeployment() //创建一个部署对象
                .name("审批流") //添加部署的名称
                .addClasspathResource("workflow/testActiviti.bpmn")//从classpath加载资源文件 一次只能加载一个文件
                .addClasspathResource("workflow/testActiviti.png")
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
        String processDefinitionKey = "myProcess";
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
        String assignee = "李四";//张三 李四
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
        String taskId = "40002"; //2505 5002  15005  20002    32505  40002
        processEngine.getTaskService() //与任务相关的service
        .complete(taskId); //完成任务
        logger.info("完成任务，任务id:"+taskId); //完成任务，任务id:5002

    }

    /**
     * 查询流程定义
     */
    @Test
    public void findProcessDefinition(){
        List<ProcessDefinition> list = processEngine.getRepositoryService()//与流程定义和部署相关的service
                .createProcessDefinitionQuery()//创建一个流程定义的查询
                /** 指定查询条件， where 条件**/
//        .deploymentId(deploymentId)//使用部署对象ID查询
//        .processDefinitionId(processDefinitionId)//使用流程定义Id查询
//        .processDefinitionKey(processDefinitionKey) //使用流程定义key查询
//        .processDefinitionName(processDefinionName) //使用流程定义Name查询

                /** 排序**/
                .orderByProcessDefinitionVersion().asc()//按照版本的升序排列
//        .orderByProcessDefinitionName().desc()//按照流程定义的名称降序排列
                /** 返回的结果**/
                .list();//返回一个集合列表，封装流程定义
//        .singleResult();//返回唯一结果集
//        .count();//返回结果集数量
//       .listPage(firstResult,maxResults);//分页查询
        if(list!=null && list.size()>0){
            for(ProcessDefinition pd:list){
                System.out.println("流程定义ID:"+pd.getId());//流程定义的key+版本+随机生成数
                System.out.println("流程定义名称："+pd.getName());//bpmn文件中的names属性值
                System.out.println("流程定义的KEY:"+pd.getKey());//bpmn文件中的id属性值
                System.out.println("流程定义的版本："+pd.getVersion());//流程定义的key相同的情况下 版本升级
                System.out.println("资源名称bpmn文件："+pd.getResourceName());
                System.out.println("资源名称png文件："+pd.getDiagramResourceName());
                System.out.println("部署对象ID:"+pd.getDeploymentId());
                System.out.println("#######################################################");
            }
        }
    }

    /**
     * 删除流程定义
     */
    @Test
    public void deleteaProcessDefinition(){
        String deploymentId = "65001";
        //使用部署ID 完成删除
//        processEngine.getRepositoryService()//
//        .deleteDeployment(deploymentId); //不能级联删除，只能删除没有启动的流程，如果流程启动就会抛异常

        //不管流程是否启动，都能删除
        processEngine.getRepositoryService()//

        .deleteDeployment(deploymentId,true);//默认是false  实现级联删除。
    }

    /**
     * 查看流程图
     */
    @Test
    public void viewflow() throws IOException {
        //将生成的图片放到文件夹下
        String deploymentId = "1";
        String resourceName = "";
        //获取图片资源名称
        List<String> list = processEngine.getRepositoryService()
                .getDeploymentResourceNames(deploymentId);
        if(list!=null && list.size()>0){
            for(String name:list){
                if(name.indexOf(".png")>=0){
                    resourceName = name;
                }
            }
        }
        InputStream in = processEngine.getRepositoryService()//
                .getResourceAsStream(deploymentId, resourceName);
        //将图片生成到d盘的目录下
        File file = new File("D:/"+resourceName);
        //将输入流图片写入到d盘
        FileUtils.copyInputStreamToFile(in,file);
    }

    /**
     * 附加功能 查询最新版本的流程
     */
    @Test
    public void findLastVersionProcessDefinition(){
        List<ProcessDefinition> list = processEngine.getRepositoryService()//
                .createProcessDefinitionQuery()
                .orderByProcessDefinitionVersion().asc()//是用流程定义的版本升序排序
                .list();
        Map<String , ProcessDefinition> map = new LinkedHashMap<String , ProcessDefinition>();
        if(list!=null && list.size()>0){
            for(ProcessDefinition pd:list){
                map.put(pd.getKey(),pd);
            }
        }
        List<ProcessDefinition> pdlist = new ArrayList<ProcessDefinition>(map.values());
        if(pdlist!=null && pdlist.size()>0){
            for(ProcessDefinition pd:pdlist){
                System.out.println( "流程定义ID:"+pd.getId());//流程定义的key+版本+随机生成数
                System.out.println( "流程定义名称："+pd.getName());//bpmn文件中的names属性值
                System.out.println( "流程定义的KEY:"+pd.getKey());//bpmn文件中的id属性值
                System.out.println( "流程定义的版本："+pd.getVersion());//流程定义的key相同的情况下 版本升级
                System.out.println( "资源名称bpmn文件："+pd.getResourceName());
                System.out.println( "资源名称png文件："+pd.getDiagramResourceName());
                System.out.println( "部署对象ID:"+pd.getDeploymentId());
                System.out.println( "#######################################################");
            }
        }
    }

    /**
     * 附加功能，删除流程定义（删除key相同的所有不同版本的流程定义）
     */
    public void deleteProcessDefintionByKey(){
        //流程定义的key
        String key ="myProcess";
        List<ProcessDefinition> list = processEngine.getRepositoryService()//先使用流程定义的key查询流程定义，查询出所有的版本
                .createProcessDefinitionQuery()
                .list();
        //遍历 获取每个流程定义的部署Id
        if(list!=null && list.size()>0){
            for(ProcessDefinition pd:list){
                String deploymentId = pd.getDeploymentId();
                processEngine.getRepositoryService()
                        .deleteDeployment(deploymentId,true);
            }
        }
    }

    /**
     * 查询流程状态，判断流程是否正在执行还是结束
     */
    @Test
    public void isProcessEnd(){
        String processInstanceId = "2501";
        ProcessInstance pi = processEngine.getRuntimeService()//表示正在执行的流程实例和执行对象
                .createProcessInstanceQuery()//创建流程实例查询
                .processInstanceId(processInstanceId)//使用流程实例ID
                .singleResult();//流程实例对象
        if(pi == null){
            System.out.println("流程结束");
        }else{
            System.out.println("流程没有结束");
        }
    }

    /**
     * 查询历史任务（后面讲）
     */
    @Test
    public void findHistoryTask(){
        String taskAssignee ="";
        List<HistoricTaskInstance> list = processEngine.getHistoryService()//与历史数据（历史表）相关的service
                .createHistoricTaskInstanceQuery()//创建历史任务实例查询
                .taskAssignee(taskAssignee)//指定历史任务的办理人
                .list();
        if(list!= null && list.size()>0){
            for(HistoricTaskInstance hti:list){
                System.out.println(hti.getId()+"  "+hti.getName()+" "+hti.getProcessDefinitionId()+" "+hti.getStartTime()+" "+hti.getEndTime());
            }
        }
    }

    /**
     * 查询历史流程实例（后面讲）
     */
    @Test
    public void findHistoryProcessInstance(){
        String processInstanceId = "";
        HistoricProcessInstance hpi = processEngine.getHistoryService()
                .createHistoricProcessInstanceQuery()//创建历史流程实例查询
                .processInstanceId(processInstanceId)//使用流程实例ID查询
                .singleResult();
        System.out.println(hpi.getId()+" "+hpi.getProcessDefinitionId()+" "+hpi.getStartTime()+" "+hpi.getEndTime());
    }

    /**
     * 设置流程变量
     */
    @Test
    public void setVariables(){
        //任务Id
        String taskId ="32505";//15005 20002        32505
        /** 与任务（正在执行）**/
        TaskService taskService = processEngine.getTaskService();
        /** 1. 设置流程变量 使用基本数据类型**/
//        taskService.setVariableLocal(taskId,"请假天数",5);//与任务Id绑定
//        taskService.setVariable(taskId,"请假日期",new Date());
//        taskService.setVariable(taskId,"请假原因","回家探亲--起吃饭");//select * from act_ru_variable

        /** 2. 设置流程变量使用javabean**/
        Person p = new Person(1,"张三");
        taskService.setVariable(taskId,"人员信息",p);
    }

    /**
     * 获取流程变量
     */
    @Test
    public void getVariables(){
        //任务Id
        String taskId ="32505";//15005 20002        32505
        /** 与任务（正在执行）**/
        TaskService taskService = processEngine.getTaskService();
        /** 1. 获取流程变量 使用基本数据类型**/
//        Integer days = (Integer) taskService.getVariable(taskId, "请假天数");
//        Date date = (Date) taskService.getVariable(taskId, "请假日期");
//        String reason = (String) taskService.getVariable(taskId, "请假原因");
//
//        System.out.println("请假天数:"+days);
//        System.out.println("请假日期:"+date);
//        System.out.println("请假原因:"+reason);

        /** 2. 获取流程变量，使用JavaBean类型 **/
        /**
         * 当一个javabean（实现序列）放置到流程变量中，要求javabean的属性不能再发生变化，
         * 如果发生变化，在获取的时候会抛出异常
         * 解决办法加上版本号序列号
         */

        Person p = (Person) taskService.getVariable(taskId, "人员信息");
        System.out.println(p.toString());

    }

    /** 模拟设置和获取流程变量的场景**/
    public void setAndGetVariables(){
        /** 与流程实例，执行对象（正在执行）**/
        RuntimeService runtimeService = processEngine.getRuntimeService();
        /** 与任务（正在执行）**/
        TaskService taskService = processEngine.getTaskService();

        /** 设置流程变量**/
//        runtimeService.setVariable(executionId,variableName,value);//表示执行对象id 和流程变量的名称，设置流程变量的值（一次只能设置一个值）
//        runtimeService.setVariables(executionId,variables);//表示使用执行对象ID,和map集合设置流程变量。map集合的key就是流程变量名称，map集合的value 就是流程变量的值（一次可以设置多个）

//        taskService.setVariable(taskId,variableName,value);//表示任务id 和流程变量的名称，设置流程变量的值（一次只能设置一个值）
//        taskService.setVariables(taskId,variables);//表示使用任务ID,和map集合设置流程变量。map集合的key就是流程变量名称，map集合的value 就是流程变量的值（一次可以设置多个）

//        runtimeService.startProcessInstanceByKey(processDefinitionKey,variables);//启动流程实例的时候设置流程变量，使用map 集合
//        taskService.setVariables(taskId,variables);//完成任务的时候设置流程变量，使用map

        /** 获取流程变量**/
//        runtimeService.getVariable(executionId,vaiableName);//根据执行对象id,和流程变量名称 获取流程变量值
//        runtimeService.getVariables(executionId);//使用执行对象ID,获取所有的流程变量，将流程变量放置到map集合中
//        runtimeService.getVariables(executionId，vaiableNames);//使用执行对象Id,获取流程变量的值，通过设置流程变量的名称存放到集合中，获取指定流程变量名称的流程变量的值，值存放到map集合中


//        taskService.getVariable(taskId,vaiableName);//根据任务id,和流程变量名称 获取流程变量值
//        taskService.getVariables(taskId);//使用任务ID,获取所有的流程变量，将流程变量放置到map集合中
//        taskService.getVariables(taskId，vaiableNames);//使用任务Id,获取流程变量的值，通过设置流程变量的名称存放到集合中，获取指定流程变量名称的流程变量的值，值存放到map集合中


    }

    /**
     * 查询流程变量的历史表
     */
    @Test
    public void findHistoryProcessVariables(){
        List<HistoricVariableInstance> list = processEngine.getHistoryService()//
                .createHistoricVariableInstanceQuery()//创建一个历史的流程变量查询对象
                .variableName("请假天数")
                .list();
        if(list!=null && list.size()>0){
            for(HistoricVariableInstance hvi :list){
                System.out.println(hvi.getId()+" "+hvi.getProcessInstanceId()+" "+hvi.getVariableTypeName()+" "+hvi.getVariableName()+" "+hvi.getValue());
                System.out.println("######################################################");
            }
        }
    }

    /**
     * 历史流程实例
     */
    @Test
    public void findHistoryProcessInstance_(){
        String processInstanceId ="15001";
        HistoricProcessInstance hpi = processEngine.getHistoryService()//
                .createHistoricProcessInstanceQuery()//创建一个历史的流程实例查询对象
                .processInstanceId(processInstanceId)//使用流程实例ID查询
                .singleResult();
        if(hpi!=null){
                System.out.println(hpi.getId()+" "+hpi.getProcessDefinitionId()+" "+hpi.getStartTime()+" "+hpi.getEndTime());

        }
    }

    /**
     * 查询历史活动
     */
    @Test
    public void findHistoryActiviti(){
        String processInstanceId ="15001";
        List<HistoricActivityInstance> list = processEngine.getHistoryService()
                .createHistoricActivityInstanceQuery()//创建历史活动实例查询对象
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime().asc()
                .list();
        if(list!=null && list.size()>0){
            for(HistoricActivityInstance hai:list){
                System.out.println(hai.getId()+" "+hai.getProcessInstanceId()+ " "+hai.getStartTime()+" "+hai.getEndTime()+" "+hai.getDurationInMillis());
                System.out.println("####################################################");
            }
        }
    }

    /**
     * 查询历任务
     */
    @Test
    public void findHistoryTask_(){
//        String taskAssignee ="";
        String processInstanceId = "15001";
        List<HistoricTaskInstance> list = processEngine.getHistoryService()//与历史数据（历史表）相关的service
                .createHistoricTaskInstanceQuery()//创建历史任务实例查询
//                .taskAssignee(taskAssignee)//指定历史任务的办理人
                .processInstanceId(processInstanceId)
                .orderByHistoricTaskInstanceStartTime().asc()
                .list();
        if(list!= null && list.size()>0){
            for(HistoricTaskInstance hti:list){
                System.out.println(hti.getId()+"  "+hti.getName()+" "+hti.getProcessDefinitionId()+" "+hti.getStartTime()+" "+hti.getEndTime());
            }
        }
    }
}
