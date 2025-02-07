package com.techacademy.service;

import java.time.LocalDateTime;
//import java.time.LocalDateTime;
import java.util.List;
//import java.util.Optional;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.techacademy.constants.ErrorKinds;
//import com.techacademy.constants.ErrorKinds;
//import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
//import com.techacademy.repository.EmployeeRepository;
import com.techacademy.repository.ReportRepository;

import jakarta.transaction.Transactional;

//import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    // 日報一覧表示処理
    public List<Report> findAll() {
        return reportRepository.findAll();
    }

    // 日報保存
    @Transactional
    public ErrorKinds save(Report report) {

        if (validateReport(report) == ErrorKinds.DATECHECK_ERROR) {
            return ErrorKinds.DATECHECK_ERROR;
        }

        report.setDeleteFlg(false);
        LocalDateTime now = LocalDateTime.now();
        report.setCreatedAt(now);
        report.setUpdatedAt(now);

        reportRepository.save(report);
        return ErrorKinds.SUCCESS;
    }

    // 同一日付・同一ユーザーのレポートが存在するかチェック
    public ErrorKinds validateReport(Report report) {
        return reportRepository.existsByReportDateAndEmployee(report.getReportDate(), report.getEmployee())
               ? ErrorKinds.DATECHECK_ERROR
               : ErrorKinds.SUCCESS;
    }
}
