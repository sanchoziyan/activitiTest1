package com.demo.activiti.task;


import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

/**
 * 使用类指定流程任务的办理人
 */
public class TaskListenerImpl implements TaskListener {

    /** 用来指定任务的办理人**/
    @Override
    public void notify(DelegateTask delegateTask) {
        //指定个人任务的办理人，也可以指定组任务的办理人
        //个人任务：通过类去查询数据库，将下一个任务的办理人查询获取，然后通过setAssignee() 的方法指定任务的办理人
        String assignee ="灭绝师太";
        delegateTask.setAssignee(assignee);
    }
}
