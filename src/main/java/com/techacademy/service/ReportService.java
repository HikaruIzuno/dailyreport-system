package com.techacademy.service;

import java.time.LocalDateTime;
//import java.time.LocalDateTime;
import java.util.List;
//import java.util.Optional;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
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

    // 1件を検索
    public Report findById(Long Id) {
        try {
            //Long reportId = Long.parseLong(Id);
            return reportRepository.findById(Id).orElse(null);
        } catch (NumberFormatException e) {
            return null; // IDが数値でない場合は null を返す
        }
    }

    // 従業員削除
    @Transactional
    public ErrorKinds delete(Long id, UserDetail userDetail) {


        Report report = findById(id);
        LocalDateTime now = LocalDateTime.now();
        report.setUpdatedAt(now);
        report.setDeleteFlg(true);

        return ErrorKinds.SUCCESS;
    }

    // 更新処理
    @Transactional
    public ErrorKinds update(Report report) {
        if (report.getId() != null) {

            // 既存の従業員情報を取得
            Report existingReport = findById(report.getId());
            if (existingReport == null) {
                return ErrorKinds.CHECK_OK; // 存在しない場合のエラー処理
            }


            // date,title,contentが変更されていたら更新
            boolean isUpdated = false;

            if (report.getReportDate() != null && !report.getReportDate().equals(existingReport.getReportDate())) {
                existingReport.setReportDate(report.getReportDate());
                isUpdated = true;
            }

            if (report.getTitle() != null && !report.getTitle().equals(existingReport.getTitle())) {
                existingReport.setTitle(report.getTitle());
                isUpdated = true;
            }

            if (report.getContent() != null && !report.getContent().equals(existingReport.getContent())) {
                existingReport.setContent(report.getContent());
                isUpdated = true;
            }


            // 更新がある場合のみデータベースに保存
            if (isUpdated) {
                existingReport.setUpdatedAt(LocalDateTime.now());
                reportRepository.save(existingReport);
            }

            return ErrorKinds.SUCCESS;
        }
        return ErrorKinds.CHECK_OK;

    }


}
