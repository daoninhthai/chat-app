package com.daoninhthai.chatapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "redirect:/chat";
    }

    @GetMapping("/chat")
    public String chat(@RequestParam(defaultValue = "1") Long roomId,
                       Principal principal,
                       Model model) {
        model.addAttribute("username", principal.getName());
        model.addAttribute("roomId", roomId);
        model.addAttribute("roomName", "General");
        return "chat";
    }
}
