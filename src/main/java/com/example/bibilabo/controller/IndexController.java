package com.example.bibilabo.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Hidden // 在 Swagger 文档中隐藏这个纯用于页面跳转的内部控制器
public class IndexController {

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }
}