package com.example.app;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class GreetingController {

    private final GreetingService svc;

    public GreetingController(GreetingService svc) {
        this.svc = svc;
    }

    @GetMapping("/greet")
    public String greet(@RequestParam(required = false) String name) {
        return svc.greet(name);
    }
}