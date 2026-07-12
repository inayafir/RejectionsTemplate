package com.ursc.chss.controller;

import com.ursc.chss.dto.LetterFormDto;
import com.ursc.chss.model.Employee;
import com.ursc.chss.model.GeneratedLetter;
import com.ursc.chss.model.RejectionReason;
import com.ursc.chss.repository.EmployeeRepository;
import com.ursc.chss.repository.GeneratedLetterRepository;
import com.ursc.chss.repository.RejectionReasonRepository;
import com.ursc.chss.service.EmployeeService;
import com.ursc.chss.service.LetterService;
import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final EmployeeService employeeService;
    private final LetterService letterService;
    private final EmployeeRepository employeeRepository;
    private final GeneratedLetterRepository generatedLetterRepository;
    private final RejectionReasonRepository rejectionReasonRepository;

    public HomeController(EmployeeService employeeService,
                          LetterService letterService,
                          EmployeeRepository employeeRepository,
                          GeneratedLetterRepository generatedLetterRepository,
                          RejectionReasonRepository rejectionReasonRepository) {
        this.employeeService = employeeService;
        this.letterService = letterService;
        this.employeeRepository = employeeRepository;
        this.generatedLetterRepository = generatedLetterRepository;
        this.rejectionReasonRepository = rejectionReasonRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("letterForm", new LetterFormDto());
        model.addAttribute("rejectionReasons", letterService.getAllRejectionReasons());
        return "generate";
    }

    @GetMapping("/generate")
    public String generateForm(Model model) {
        model.addAttribute("letterForm", new LetterFormDto());
        model.addAttribute("rejectionReasons", letterService.getAllRejectionReasons());
        return "generate";
    }

    @GetMapping("/api/employees/search")
    @ResponseBody
    public List<Employee> searchEmployees(@RequestParam("q") String query) {
        return employeeService.searchEmployees(query);
    }

    @GetMapping("/api/employees/{staffId}")
    @ResponseBody
    public Employee getEmployee(@PathVariable String staffId) {
        return employeeService.getEmployeeById(staffId);
    }

    @PostMapping("/generate")
    public String generateLetter(@ModelAttribute("letterForm") LetterFormDto dto,
                                  BindingResult result, Model model) {
        if (dto.getStaffId() == null || dto.getStaffId().trim().isEmpty()) {
            model.addAttribute("error", "Please search and select an employee.");
            model.addAttribute("rejectionReasons", letterService.getAllRejectionReasons());
            return "generate";
        }

        if (dto.getSelectedReasonIds() != null) {
            dto.setSelectedReasonIds(
                dto.getSelectedReasonIds().stream()
                    .filter(id -> id != null)
                    .collect(Collectors.toList())
            );
        }

        if (dto.getSelectedReasonIds() == null || dto.getSelectedReasonIds().isEmpty()) {
            if (dto.getCustomReasons() == null || dto.getCustomReasons().stream().allMatch(s -> s == null || s.trim().isEmpty())) {
                model.addAttribute("error", "Please select or enter at least one rejection reason.");
                model.addAttribute("rejectionReasons", letterService.getAllRejectionReasons());
                return "generate";
            }
        }

        try {
            GeneratedLetter letter = letterService.generateLetter(dto);
            return "redirect:/view/" + letter.getLetterId();
        } catch (Exception e) {
            model.addAttribute("error", "Error generating letter: " + e.getMessage());
            model.addAttribute("rejectionReasons", letterService.getAllRejectionReasons());
            return "generate";
        }
    }

    @GetMapping("/view/{id}")
    public String viewLetter(@PathVariable Long id, Model model) {
        GeneratedLetter letter = letterService.getLetterById(id);
        if (letter == null) {
            return "redirect:/?error=Letter not found";
        }
        model.addAttribute("letter", letter);
        model.addAttribute("reasons", List.of(letter.getSelectedReasons().split("\\|\\|")));
        return "view-letter";
    }

    @GetMapping("/edit/{id}")
    public String editLetter(@PathVariable Long id, Model model) {
        GeneratedLetter letter = letterService.getLetterById(id);
        if (letter == null) {
            return "redirect:/?error=Letter not found";
        }

        LetterFormDto dto = new LetterFormDto();
        dto.setLetterId(letter.getLetterId());
        dto.setStaffId(letter.getEmployee().getStaffId());
        dto.setEmployeeName(letter.getEmployee().getEmployeeName());
        dto.setAddressLine1(letter.getEmployee().getAddressLine1());
        dto.setAddressLine2(letter.getEmployee().getAddressLine2());
        dto.setLocality(letter.getEmployee().getLocality());
        dto.setCity(letter.getEmployee().getCity());
        dto.setPincode(letter.getEmployee().getPincode());
        dto.setIssueDate(letter.getIssueDate().toString());
        dto.setAmount(letter.getAmount());

        if (letter.getMedicalExpenseDates() != null && !letter.getMedicalExpenseDates().isEmpty()) {
            dto.setMedicalExpenseDates(List.of(letter.getMedicalExpenseDates().split(",\\s*")));
        }

        if (letter.getSelectedReasonIds() != null && !letter.getSelectedReasonIds().isEmpty()) {
            dto.setSelectedReasonIds(
                    Arrays.stream(letter.getSelectedReasonIds().split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList())
            );
        }

        if (letter.getCustomReasons() != null && !letter.getCustomReasons().isEmpty()) {
            dto.setCustomReasons(List.of(letter.getCustomReasons().split("\\|\\|")));
        }

        model.addAttribute("letterForm", dto);
        model.addAttribute("rejectionReasons", letterService.getAllRejectionReasons());
        model.addAttribute("editMode", true);
        return "generate";
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadLetter(@PathVariable Long id) {
        GeneratedLetter letter = letterService.getLetterById(id);
        if (letter == null || letter.getPdfPath() == null) {
            return ResponseEntity.notFound().build();
        }

        File file = new File(letter.getPdfPath());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        String filename = "CHSS_REJECTION_" + letter.getEmployee().getStaffId() +
                          "_" + letter.getLetterId() + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    @GetMapping("/print/{id}")
    public ResponseEntity<Resource> printLetter(@PathVariable Long id) {
        GeneratedLetter letter = letterService.getLetterById(id);
        if (letter == null || letter.getPdfPath() == null) {
            return ResponseEntity.notFound().build();
        }

        File file = new File(letter.getPdfPath());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"letter.pdf\"")
                .body(resource);
    }

    @PostMapping("/regenerate/{id}")
    public String regenerateLetter(@PathVariable Long id) {
        GeneratedLetter existing = letterService.getLetterById(id);
        if (existing == null) {
            return "redirect:/history?error=Letter not found";
        }

        LetterFormDto dto = new LetterFormDto();
        dto.setStaffId(existing.getEmployee().getStaffId());
        dto.setIssueDate(existing.getIssueDate().toString());
        dto.setAmount(existing.getAmount());

        if (existing.getMedicalExpenseDates() != null && !existing.getMedicalExpenseDates().isEmpty()) {
            dto.setMedicalExpenseDates(List.of(existing.getMedicalExpenseDates().split(",\\s*")));
        }

        if (existing.getSelectedReasonIds() != null && !existing.getSelectedReasonIds().isEmpty()) {
            dto.setSelectedReasonIds(
                    Arrays.stream(existing.getSelectedReasonIds().split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList())
            );
        }

        if (existing.getCustomReasons() != null && !existing.getCustomReasons().isEmpty()) {
            dto.setCustomReasons(List.of(existing.getCustomReasons().split("\\|\\|")));
        }

        try {
            GeneratedLetter letter = letterService.generateLetter(dto);
            return "redirect:/view/" + letter.getLetterId();
        } catch (Exception e) {
            return "redirect:/?error=" + e.getMessage();
        }
    }
}
