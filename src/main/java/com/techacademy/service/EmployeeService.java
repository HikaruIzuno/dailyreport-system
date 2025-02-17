package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.repository.EmployeeRepository;
import com.techacademy.repository.ReportRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReportRepository reportRepository;

    public EmployeeService(EmployeeRepository employeeRepository, ReportRepository reportRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.reportRepository = reportRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 従業員保存
    @Transactional
    public ErrorKinds save(Employee employee) {

        // パスワードチェック
        ErrorKinds result = employeePasswordCheck(employee);
        if (ErrorKinds.CHECK_OK != result) {
            return result;
        }

        // 従業員番号重複チェック
        if (findByCode(employee.getCode()) != null) {
            return ErrorKinds.DUPLICATE_ERROR;
        }

        employee.setDeleteFlg(false);

        LocalDateTime now = LocalDateTime.now();
        employee.setCreatedAt(now);
        employee.setUpdatedAt(now);

        employeeRepository.save(employee);
        return ErrorKinds.SUCCESS;
    }

    // 更新処理
    @Transactional
    public ErrorKinds update(Employee employee) {
        if (employee.getCode() != null) {

            // 既存の従業員情報を取得
            Employee existingEmployee = findByCode(employee.getCode());
            if (existingEmployee == null) {
                return ErrorKinds.CHECK_OK; // 存在しない場合のエラー処理
            }

            // パスワードが空欄の場合、既存のパスワードを保持
            if (employee.getPassword() == null || employee.getPassword().isEmpty()) {
                employee.setPassword(existingEmployee.getPassword());
            } else {
                // パスワードが入力されている場合のみパスワードチェックを実施
                ErrorKinds result = employeePasswordCheck(employee);
                if (ErrorKinds.CHECK_OK != result) {
                    return result;
                }
            }

            // name と role が変更されていたら更新
            boolean isUpdated = false;

            if (employee.getName() != null && !employee.getName().equals(existingEmployee.getName())) {
                existingEmployee.setName(employee.getName());
                isUpdated = true;
            }

            if (employee.getRole() != null && !employee.getRole().equals(existingEmployee.getRole())) {
                existingEmployee.setRole(employee.getRole());
                isUpdated = true;
            }

            if (employee.getPassword() != null && !employee.getPassword().equals(existingEmployee.getPassword())) {
                existingEmployee.setPassword(employee.getPassword());
                isUpdated = true;
            }

            // 更新がある場合のみデータベースに保存
            if (isUpdated) {
                existingEmployee.setUpdatedAt(LocalDateTime.now());
                employeeRepository.save(existingEmployee);
            }

            return ErrorKinds.SUCCESS;
        }
        return ErrorKinds.CHECK_OK;

    }
    // 従業員削除
    @Transactional
    public ErrorKinds delete(String code, UserDetail userDetail) {

        // 自分を削除しようとした場合はエラーメッセージを表示
        if (code.equals(userDetail.getEmployee().getCode())) {
            return ErrorKinds.LOGINCHECK_ERROR;
        }
        Employee employee = findByCode(code);
        if (employee == null) {
            return ErrorKinds.CHECK_OK; // 既に削除済みの場合のエラー処理
        }
        LocalDateTime now = LocalDateTime.now();
        employee.setUpdatedAt(now);
        employee.setDeleteFlg(true);

        List<Report> reportList = reportRepository.findByEmployee(employee);
        reportRepository.deleteAll(reportList);

        return ErrorKinds.SUCCESS;
    }

    // 従業員一覧表示処理
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    // 1件を検索
    public Employee findByCode(String code) {
        // findByIdで検索
        Optional<Employee> option = employeeRepository.findById(code);
        // 取得できなかった場合はnullを返す
        Employee employee = option.orElse(null);
        return employee;
    }

    // 従業員パスワードチェック 一時的にPublicに変更
    public ErrorKinds employeePasswordCheck(Employee employee) {

        // 従業員パスワードの半角英数字チェック処理
        if (isHalfSizeCheckError(employee)) {

            return ErrorKinds.HALFSIZE_ERROR;
        }

        // 従業員パスワードの8文字～16文字チェック処理
        if (isOutOfRangePassword(employee)) {

            return ErrorKinds.RANGECHECK_ERROR;
        }

        employee.setPassword(passwordEncoder.encode(employee.getPassword()));

        return ErrorKinds.CHECK_OK;
    }

    // 従業員パスワードの半角英数字チェック処理
    private boolean isHalfSizeCheckError(Employee employee) {

        // 半角英数字チェック
        Pattern pattern = Pattern.compile("^[A-Za-z0-9]+$");
        Matcher matcher = pattern.matcher(employee.getPassword());
        return !matcher.matches();
    }

    // 従業員パスワードの8文字～16文字チェック処理
    public boolean isOutOfRangePassword(Employee employee) {

        // 桁数チェック
        int passwordLength = employee.getPassword().length();
        return passwordLength < 8 || 16 < passwordLength;
    }

}