package com.techacademy.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByEmployee(Employee employee);

 // 指定した日付と従業員でレポートが存在するかチェック
    @Query("SELECT COUNT(r) > 0 FROM Report r WHERE r.reportDate = :reportDate AND r.employee = :employee ")
    boolean existsByReportDateAndEmployee(@Param("reportDate") LocalDate reportDate,
                                          @Param("employee") Employee employee,
                                          @Param("id") Long id);
}