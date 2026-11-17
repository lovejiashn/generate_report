package com.jiashn.generateReport.controller;

import com.jiashn.generateReport.domain.ReportConfig;
import com.jiashn.generateReport.service.ApachePoitlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public Map<String, Object> generateCharts() {
        poitlService.generateCharts();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "报表生成成功");
        return result;
    }

    @GetMapping("/config.do")
    public ReportConfig config() {
        return poitlService.getDefaultConfig();
    }

    @GetMapping("/placeholders.do")
    public List<Map<String, String>> placeholders() {
        return poitlService.getPlaceholders();
    }

    @GetMapping("/template.do")
    public void template(HttpServletResponse response) throws IOException {
        byte[] bytes = poitlService.getDefaultTemplate();
        String fileName = URLEncoder.encode("报表默认模板.docx", StandardCharsets.UTF_8.name());
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
        response.getOutputStream().flush();
    }

    @GetMapping("/download.do")
    public void download(HttpServletResponse response) throws IOException {
        writeDocx(response, poitlService.generateReportBytes());
    }

    @PostMapping("/preview.do")
    public void preview(@RequestBody ReportConfig config, HttpServletResponse response) throws IOException {
        writeDocx(response, poitlService.generateReportBytes(config));
    }

    private void writeDocx(HttpServletResponse response, byte[] bytes) throws IOException {
        String fileName = URLEncoder.encode("月度通报函生成报告.docx", StandardCharsets.UTF_8.name());
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
        response.getOutputStream().flush();
    }
}
