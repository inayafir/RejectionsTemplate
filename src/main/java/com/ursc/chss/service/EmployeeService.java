package com.ursc.chss.service;

import com.ursc.chss.model.Employee;
import com.ursc.chss.model.GeneratedLetter;
import com.ursc.chss.repository.EmployeeRepository;
import com.ursc.chss.repository.GeneratedLetterRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for employee management operations.
 */
@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final GeneratedLetterRepository generatedLetterRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           GeneratedLetterRepository generatedLetterRepository) {
        this.employeeRepository = employeeRepository;
        this.generatedLetterRepository = generatedLetterRepository;
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee getEmployeeById(String staffId) {
        return employeeRepository.findById(staffId).orElse(null);
    }

    public List<Employee> searchEmployees(String query) {
        return employeeRepository.findByStaffIdContainingIgnoreCaseOrEmployeeNameContainingIgnoreCase(query, query);
    }

    public boolean existsByStaffId(String staffId) {
        return employeeRepository.existsByStaffId(staffId);
    }

    @Transactional
    public Employee saveEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    @Transactional
    public void deleteEmployee(String staffId) {
        generatedLetterRepository.findByEmployeeStaffIdContainingIgnoreCase(staffId)
                .forEach(letter -> {
                    try {
                        java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(letter.getPdfPath()));
                    } catch (java.io.IOException e) {
                        // ignore
                    }
                    generatedLetterRepository.delete(letter);
                });
        employeeRepository.deleteById(staffId);
    }

    /**
     * Imports employees from an .xlsx file.
     * Inserts new employees, updates existing ones, and ignores duplicates.
     */
    @Transactional
    public ImportResult importEmployees(MultipartFile file) throws IOException {
        ImportResult result = new ImportResult();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String staffId = getCellStringValue(row.getCell(0));
                if (staffId == null || staffId.trim().isEmpty()) {
                    result.incrementSkipped();
                    continue;
                }

                staffId = staffId.trim();
                if (employeeRepository.existsByStaffId(staffId)) {
                    Employee existing = employeeRepository.findById(staffId).get();
                    updateEmployeeFromRow(existing, row);
                    employeeRepository.save(existing);
                    result.incrementUpdated();
                } else {
                    Employee emp = createEmployeeFromRow(staffId, row);
                    employeeRepository.save(emp);
                    result.incrementInserted();
                }
            }
        }
        return result;
    }

    /**
     * Exports all employees to an .xlsx file.
     */
    public byte[] exportEmployees() throws IOException {
        List<Employee> employees = employeeRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Employees");
            String[] headers = {"Staff ID", "Employee Name", "Department", "Designation",
                                "Phone", "Email", "Address Line 1", "Address Line 2",
                                "Locality", "City", "Pincode"};

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Employee emp : employees) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(emp.getStaffId());
                row.createCell(1).setCellValue(emp.getEmployeeName());
                row.createCell(2).setCellValue(emp.getDepartment());
                row.createCell(3).setCellValue(emp.getDesignation());
                row.createCell(4).setCellValue(emp.getPhone());
                row.createCell(5).setCellValue(emp.getEmail());
                row.createCell(6).setCellValue(emp.getAddressLine1());
                row.createCell(7).setCellValue(emp.getAddressLine2());
                row.createCell(8).setCellValue(emp.getLocality());
                row.createCell(9).setCellValue(emp.getCity());
                row.createCell(10).setCellValue(emp.getPincode());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        }
    }

    private Employee createEmployeeFromRow(String staffId, Row row) {
        Employee emp = new Employee();
        emp.setStaffId(staffId);
        emp.setEmployeeName(getCellStringValue(row.getCell(1)));
        emp.setDepartment(getCellStringValue(row.getCell(2)));
        emp.setDesignation(getCellStringValue(row.getCell(3)));
        emp.setPhone(getCellStringValue(row.getCell(4)));
        emp.setEmail(getCellStringValue(row.getCell(5)));
        emp.setAddressLine1(getCellStringValue(row.getCell(6)));
        emp.setAddressLine2(getCellStringValue(row.getCell(7)));
        emp.setLocality(getCellStringValue(row.getCell(8)));
        emp.setCity(getCellStringValue(row.getCell(9)));
        emp.setPincode(getCellStringValue(row.getCell(10)));
        return emp;
    }

    private void updateEmployeeFromRow(Employee emp, Row row) {
        if (getCellStringValue(row.getCell(1)) != null)
            emp.setEmployeeName(getCellStringValue(row.getCell(1)));
        if (getCellStringValue(row.getCell(2)) != null)
            emp.setDepartment(getCellStringValue(row.getCell(2)));
        if (getCellStringValue(row.getCell(3)) != null)
            emp.setDesignation(getCellStringValue(row.getCell(3)));
        if (getCellStringValue(row.getCell(4)) != null)
            emp.setPhone(getCellStringValue(row.getCell(4)));
        if (getCellStringValue(row.getCell(5)) != null)
            emp.setEmail(getCellStringValue(row.getCell(5)));
        if (getCellStringValue(row.getCell(6)) != null)
            emp.setAddressLine1(getCellStringValue(row.getCell(6)));
        if (getCellStringValue(row.getCell(7)) != null)
            emp.setAddressLine2(getCellStringValue(row.getCell(7)));
        if (getCellStringValue(row.getCell(8)) != null)
            emp.setLocality(getCellStringValue(row.getCell(8)));
        if (getCellStringValue(row.getCell(9)) != null)
            emp.setCity(getCellStringValue(row.getCell(9)));
        if (getCellStringValue(row.getCell(10)) != null)
            emp.setPincode(getCellStringValue(row.getCell(10)));
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    /**
     * Result of an employee import operation.
     */
    public static class ImportResult {
        private int inserted = 0;
        private int updated = 0;
        private int skipped = 0;

        public void incrementInserted() { inserted++; }
        public void incrementUpdated() { updated++; }
        public void incrementSkipped() { skipped++; }

        public int getInserted() { return inserted; }
        public int getUpdated() { return updated; }
        public int getSkipped() { return skipped; }
        public int getTotal() { return inserted + updated + skipped; }
    }
}
