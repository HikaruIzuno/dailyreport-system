package com.techacademy.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {

 // 指定した日付と従業員でレポートが存在するかチェック
    boolean existsByReportDateAndEmployee(LocalDate reportDate, Employee employee);
}