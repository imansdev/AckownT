package com.imansdev.ackownt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.imansdev.ackownt.service.MainService;

@RestController
@RequestMapping("/api/v1")
public class MainController {

    @Autowired
    private MainService mainService;


}
