package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "confirm")
public class Confirm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ObjectID", nullable = false)
    private Integer id;

    @Size(max = 100)
    @NotNull
    @Column(name = "Company", nullable = false, length = 100)
    private String company;

    @Size(max = 100)
    @NotNull
    @Column(name = "Position", nullable = false, length = 100)
    private String position;

    @Size(max = 50)
    @NotNull
    @Column(name = "City", nullable = false, length = 50)
    private String city;

    @NotNull
    @Column(name = "Salary", nullable = false, precision = 10, scale = 2)
    private int salary;

    @Size(max = 50)
    @NotNull
    @Column(name = "Type", nullable = false, length = 50)
    private String type;

    @Size(max = 50)
    @NotNull
    @Column(name = "Education", nullable = false, length = 50)
    private String education;

    @Size(max = 100)
    @NotNull
    @Column(name = "Industry", nullable = false, length = 100)
    private String industry;

    @Lob
    @Column(name = "Remarks")
    private String remarks;

    @Size(max = 100)
    @NotNull
    @Column(name = "create_user", nullable = false, length = 100)
    private String createUser;

    @Override
    public String toString() {
        return "ID: " + id + ", 公司: " + company + ", 职位: " + position + ", 城市: " + city +
                ", 薪资: " + salary + "元, 类型: " + type + ", 学历: " + education +
                ", 行业: " + industry + ", 备注: " + remarks + ", 创建用户: " + createUser;
    }
}
