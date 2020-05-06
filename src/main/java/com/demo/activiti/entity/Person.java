package com.demo.activiti.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class Person implements Serializable{

    private static final long serialVersionUID = 1274388355524611409L;
    private Integer id;
    private String pname;
    private int sex;

    public Person(Integer id, String pname) {
        this.id = id;
        this.pname = pname;
    }
}
