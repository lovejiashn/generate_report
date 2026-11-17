package com.jiashn.generateReport.domain;

import lombok.Data;

import java.util.List;

/**
 * @description: Word 报表配置，由前端 JSON 传入
 */
@Data
public class ReportConfig {

    private String title;
    private StyledText typeContent;
    private PictureConfig picture;
    private TableConfig showTable;
    private ListConfig numList;
    private List<ChartConfig> charts;
    private java.util.Map<String, String> chartImages;

    @Data
    public static class StyledText {
        private String color;
        private String text;
    }

    @Data
    public static class PictureConfig {
        private Integer width;
        private Integer height;
        private String text;
        private String url;
    }

    @Data
    public static class TableConfig {
        private List<String> headers;
        private List<List<String>> rows;
    }

    @Data
    public static class ListConfig {
        private String style;
        private List<String> items;
    }

    @Data
    public static class ChartConfig {
        private String labelName;
        private String title;
        private String type;
        private List<String> categories;
        private List<SeriesConfig> series;
    }

    @Data
    public static class SeriesConfig {
        private String name;
        private List<Number> data;
        private String comboType;
    }
}
