package com.breakingns.SomosTiendaMas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {
    @GetMapping("/__test_template")
    public String test() {
        return "test";
    }
}