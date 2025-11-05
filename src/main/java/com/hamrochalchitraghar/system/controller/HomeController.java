package com.hamrochalchitraghar.system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

@Controller
public class HomeController {

    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("message", "🎥 Hamro Chalchitraghar System is running successfully!");
        return "index";
    }
}
