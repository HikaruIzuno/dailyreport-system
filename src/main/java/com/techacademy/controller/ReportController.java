package com.techacademy.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.service.ReportService;
import com.techacademy.service.UserDetail;

@Controller
@RequestMapping("reports")
public class ReportController {
    @Autowired
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // 日報一覧画面
    @GetMapping
    public String list(Model model, @AuthenticationPrincipal UserDetail userDetail) {
        // すべてのレポートを取得
        List<Report> allReports = reportService.findAll();
        List<Report> filteredReports;
        // ユーザーのロールを取得
        String role = userDetail.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .findFirst()
                                .orElse("");

        if ("ADMIN".equals(role)) {
            // 管理者の場合はすべてのレポートを表示
            filteredReports = allReports;
        } else {
            // 一般ユーザーの場合は、自分のレポートのみを表示
            String username = userDetail.getEmployee().getName();
            filteredReports = allReports.stream()
                                        .filter(report -> report.getEmployee().getName().equals(username))
                                        .collect(Collectors.toList());
        }
        model.addAttribute("listSize", filteredReports.size());
        model.addAttribute("reportList", filteredReports);
        return "reports/list";
    }

    // 日報新規登録画面

    @GetMapping(value = "/add")
    public String create(@AuthenticationPrincipal UserDetail userDetail, Model model) {
        Report report = new Report();
        // ログイン中のユーザーを取得
        Employee employee = userDetail.getEmployee();
        // モデルにセット
        model.addAttribute("report", report);
        model.addAttribute("employee", employee);
        return "reports/new";
    }

    // 日報新規登録処理
    @PostMapping(value = "/add")
    public String add(@Validated Report report, BindingResult res,
            @AuthenticationPrincipal UserDetail userDetail,Model model) {

        Employee employee = userDetail.getEmployee(); // ログイン中のユーザーを取得
        report.setEmployee(employee); // Report に Employee をセット

        LocalDate reportDate = report.getReportDate(); // reportDate を取得
        model.addAttribute("reportDate", reportDate); // model にセット

        // 入力エラーがある場合はフォームに戻る
        if (res.hasErrors()) {
            model.addAttribute("employee", employee);
            return "reports/new";
        }

        ErrorKinds error = reportService.validateReport(report);
        if (error == ErrorKinds.DATECHECK_ERROR) {
            model.addAttribute("employee", employee);
            model.addAttribute("reportDate", reportDate); // エラー時もセット
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DATECHECK_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.DATECHECK_ERROR));

            return "reports/new";
        }

        reportService.save(report);

        return "redirect:/reports";
    }
}
