package com.jiashn.generateReport.controller;

import com.jiashn.generateReport.service.ApachePoitlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: jiangjs
 * @description: Poi-tl生成图表
 * @date: 2022/11/23 17:08
 **/
@RestController
@RequestMapping("/poitl")
public class ApachePoitlController {

    @Autowired
    private ApachePoitlService poitlService;

    @GetMapping("/generateCharts.do")
    public void generateCharts(){
        poitlService.generateCharts();
    }
}