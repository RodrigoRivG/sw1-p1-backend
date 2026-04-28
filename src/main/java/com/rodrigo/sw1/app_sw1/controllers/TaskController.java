package com.rodrigo.sw1.app_sw1.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import com.rodrigo.sw1.app_sw1.services.TaskService;
import com.rodrigo.sw1.app_sw1.dto.TaskRequest;
import com.rodrigo.sw1.app_sw1.models.Task;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    
    @Autowired
    private TaskService taskService;

    @GetMapping("/my-tasks")
    public ResponseEntity<List<Map<String, Object>>> getMyTasks(Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(taskService.getEnrichedTasksByUser(userId));
    }

    @GetMapping("/my-tasks/pendings")
    public ResponseEntity<List<Task>> getPendingTasks(Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(taskService.getPendingByUser(userId));
    }

    @PutMapping("/{id}/completed")
    public ResponseEntity<?> completedTaks(@PathVariable String id, @RequestBody TaskRequest request) {
        try {
            return ResponseEntity.ok(taskService.completeTask(id, request));
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
