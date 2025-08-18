package com.projectone.PalliativeCare.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/patient")
public class PatientPageController {

    @GetMapping("posts")
    public String posts() {
        return "Patient main page";
    }
}
