package com.example.demo.repository;

import com.example.demo.entity.Confirm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ConfirmRepository extends JpaRepository<Confirm, Integer>, JpaSpecificationExecutor<Confirm> {
    // 你可以在这里添加其他方法定义
}
