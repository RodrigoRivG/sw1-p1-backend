package com.rodrigo.sw1.app_sw1.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;

@Controller
public class WebSocketController {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/policy/{policyId}/update")
    @SendTo("/topic/policy/{policyId}")
    public Map<String, Object> updateDiagram(
        @DestinationVariable String policyId, 
        Map<String, Object> diagram) {
            return diagram;
    }

    @MessageMapping("/policy/{policyId}/join")
    public void joinPolicy(
        @DestinationVariable String policyId,
        Map<String, Object> user) {
            messagingTemplate.convertAndSend(
                "/topic/policy/" + policyId + "/users", 
                user
            );
    }
}
