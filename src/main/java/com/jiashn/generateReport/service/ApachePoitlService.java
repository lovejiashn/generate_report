package com.jiashn.generateReport.service;

import com.jiashn.generateReport.domain.ReportConfig;

import java.util.List;
import java.util.Map;

/**
 * @author: jiangjs
 * @description:
 * @date: 2022/11/23 17:13
 **/
public interface ApachePoitlService {
    void generateCharts();

    byte[] generateReportBytes();

    ReportConfig getDefaultConfig();

    byte[] generateReportBytes(ReportConfig config);

    byte[] getDefaultTemplate();

    List<Map<String, String>> getPlaceholders();
}
