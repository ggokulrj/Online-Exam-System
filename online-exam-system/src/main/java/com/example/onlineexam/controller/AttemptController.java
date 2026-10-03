package com.example.onlineexam.controller;

import com.example.onlineexam.dto.ResultResponse;
import com.example.onlineexam.dto.StudentReport;
import com.example.onlineexam.dto.SubmitRequest;
import com.example.onlineexam.service.AttemptService;
import com.example.onlineexam.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attempts")
@RequiredArgsConstructor
public class AttemptController {

    private final AttemptService attemptService;
    private final ReportService reportService;

    @PostMapping("/{attemptId}/submit")
    public ResultResponse submit(@PathVariable Long attemptId, @RequestBody SubmitRequest req, Authentication auth) {
        return attemptService.submit(auth.getName(), attemptId, req);
    }

    @GetMapping("/my")
    public StudentReport myReport(Authentication auth) {
        return reportService.studentReport(auth.getName());
    }
}
