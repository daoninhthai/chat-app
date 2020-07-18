package com.daoninhthai.chatapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "login";
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String username,
                       @RequestParam(defaultValue = "1") Long roomId,
                       Model model) {
        model.addAttribute("username", username);
        model.addAttribute("roomId", roomId);
        model.addAttribute("roomName", "General");
        return "chat";
    }
}
