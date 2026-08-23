package com.evoting.app.controller;

import com.evoting.app.model.AuditLog;
import com.evoting.app.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
@CrossOrigin(origins = "*")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @GetMapping("/logs")
    public ResponseEntity<List<AuditLog>> getLogs() {
        return ResponseEntity.ok(auditService.getAuditLogs());
    }

    @GetMapping("/verify-chain")
    public ResponseEntity<Map<String, Object>> verifyChain() {
        return ResponseEntity.ok(auditService.verifyChainIntegrity());
    }
}
