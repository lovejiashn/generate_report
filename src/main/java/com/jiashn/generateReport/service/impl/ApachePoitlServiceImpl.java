package com.jiashn.generateReport.service.impl;

import com.deepoove.poi.data.NumbericRenderData;
import com.deepoove.poi.data.SeriesRenderData;
import com.deepoove.poi.data.TextRenderData;
import com.jiashn.generateReport.domain.*;
import com.jiashn.generateReport.enums.CharCombinationType;
import com.jiashn.generateReport.enums.PicTypeEnum;
import com.jiashn.generateReport.enums.WordContentTypeEnum;
import com.jiashn.generateReport.service.ApachePoitlService;
import com.jiashn.generateReport.util.OperateWordManage;
import org.apache.commons.lang3.tuple.Pair;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STNumberFormat;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * @author: jiangjs
 * @description:
 * @date: 2022/11/23 17:13
 **/
@Service
public class ApachePoitlServiceImpl implements ApachePoitlService {

    private static final String TEMPLATE_PATH = "static/template/demo_template.docx";
    private static final String IMAGE_TEMPLATE_PATH = "static/template/demo_template_img.docx";

    @Override
    public void generateCharts() {
        File output = new File(System.getProperty("java.io.tmpdir"), "generate_report_output.docx");
        OperateWordManage.generateWordContent(loadTemplate(), output.getAbsolutePath(), buildContents(getDefaultConfig()));
    }

    @Override
    public byte[] generateReportBytes() {
        return generateReportBytes(getDefaultConfig());
    }

    @Override
    public byte[] generateReportBytes(ReportConfig config) {
        Map<String, String> chartImages = config == null ? null : config.getChartImages();
        if (chartImages != null && !chartImages.isEmpty()) {
            return OperateWordManage.generateWordBytes(loadTemplate(IMAGE_TEMPLATE_PATH), buildImageContents(config));
        }
        return OperateWordManage.generateWordBytes(loadTemplate(), buildContents(config));
    }

    @Override
    public byte[] getDefaultTemplate() {
        try (InputStream in = new ClassPathResource(IMAGE_TEMPLATE_PATH).getInputStream()) {
            return readAllBytes(in);
        } catch (IOException e) {
            throw new RuntimeException("读取默认模板失败", e);
        }
    }

    @Override
    public List<Map<String, String>> getPlaceholders() {
        List<Map<String, String>> list = new ArrayList<>();
        list.add(placeholder("{{title}}", "标题文本", "title", "普通文本"));
        list.add(placeholder("{{typeContent}}", "带样式文本", "typeContent.color / text", "普通文本"));
        list.add(placeholder("{{@picture}}", "图片", "picture.width / height / text / url", "图片"));
        list.add(placeholder("{{#showTable}}", "表格", "showTable.headers / rows", "表格"));
        list.add(placeholder("{{*numList}}", "列表", "numList.style / items", "列表"));
        list.add(placeholder("{{@speedLine}}", "折线图", "charts[labelName=speedLine]", "图表图片"));
        list.add(placeholder("{{@investmentRatio}}", "柱状图", "charts[labelName=investmentRatio]", "图表图片"));
        list.add(placeholder("{{@areaShow}}", "面积图", "charts[labelName=areaShow]", "图表图片"));
        list.add(placeholder("{{@cycleRadio}}", "横向柱状图", "charts[labelName=cycleRadio]", "图表图片"));
        list.add(placeholder("{{@openCondition}}", "组合图", "charts[labelName=openCondition]", "图表图片"));
        return list;
    }

    private Map<String, String> placeholder(String tag, String name, String configPath, String type) {
        Map<String, String> item = new HashMap<>();
        item.put("tag", tag);
        item.put("name", name);
        item.put("configPath", configPath);
        item.put("type", type);
        return item;
    }

    private byte[] readAllBytes(InputStream in) throws IOException {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int len;
        while ((len = in.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        return bos.toByteArray();
    }

    @Override
    public ReportConfig getDefaultConfig() {
        ReportConfig config = new ReportConfig();
        config.setTitle("2022年月通报函生成报告");

        ReportConfig.StyledText styled = new ReportConfig.StyledText();
        styled.setColor("cc0000");
        styled.setText("这是带样式的内容");
        config.setTypeContent(styled);

        ReportConfig.PictureConfig picture = new ReportConfig.PictureConfig();
        picture.setWidth(200);
        picture.setHeight(160);
        picture.setText("generate_report");
        config.setPicture(picture);

        ReportConfig.TableConfig table = new ReportConfig.TableConfig();
        table.setHeaders(Arrays.asList("班级", "排名"));
        table.setRows(Arrays.asList(
                Arrays.asList("科教1班", "1"),
                Arrays.asList("幼儿3班", "6")));
        config.setShowTable(table);

        ReportConfig.ListConfig list = new ReportConfig.ListConfig();
        list.setStyle("lowerRoman");
        list.setItems(Arrays.asList("排序1", "排序2", "排序3"));
        config.setNumList(list);

        config.setCharts(Arrays.asList(
                chart("speedLine", "1-10月份全国新开工项目数量、投资额增速", "multi",
                        Arrays.asList("1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月"),
                        Arrays.asList(
                                series("项目数量", "-11.02,-19.42,-10.61,-11.41,-7.91,-5.44,-5.30,-2.75,-1.24,0.35", null),
                                series("投资额", "-12.66,-19.41,-15.16,-19.72,-17.05,-15.92,-15.10,-13.04,-10.65,-9.15", null))),
                chart("investmentRatio", "各省（自治区）直辖市新开项目数量、投资额同比情况", "multi",
                        Arrays.asList("贵州", "西藏", "黑龙江", "浙江", "湖北", "江苏", "四川", "福建", "安徽", "海南", "山西", "广西", "青海", "广东", "甘肃",
                                "云南", "宁夏", "新疆", "湖南", "北京", "河北", "山西", "山东", "内蒙古", "天津", "江西", "吉林", "河南", "重庆", "上海", "辽宁"),
                        Arrays.asList(
                                series("开工数量", "40,50,45,12,21,18,21,28,21,18,28,18,20,19,-10,-9,-10,19,39,31,20,19,-10,-9,-10,19,39,31,-10,19,39", null),
                                series("投资额", "20,-22,-12,8,-10,-14,-10,-10,-8,-2,-8,-1,-9,-21,-9,-7,-21,-10,21,-29,-50,-21,-9,-7,-21,-10,21,-29,-21,-10,21", null))),
                chart("areaShow", "国家投资额", "single",
                        Arrays.asList("俄罗斯", "加拿大", "美国", "中国"),
                        Arrays.asList(series("投资额", "17098242,9984670,9826675,9596961", "AREA"))),
                chart("cycleRadio", "工程建设项目建设周期同比情况", "multi",
                        Arrays.asList("从立项到开工的用时", "从开工到验收的用时"),
                        Arrays.asList(
                                series("2021年", "400,200", null),
                                series("2022年", "456,255", null))),
                chart("openCondition", "各省（区、市）签约项目开工情况", "multi",
                        Arrays.asList("北京", "吉林", "云南", "上海", "安徽", "浙江", "江西", "四川", "陕西", "甘肃", "江苏", "广西", "内蒙古", "福建", "天津", "海南", "黑龙江",
                                "贵州", "山东", "河北", "辽宁", "湖北", "宁夏", "广东", "重庆", "河南", "新疆", "山西", "湖南", "青海", "兵团"),
                        Arrays.asList(
                                series("未开工项目数（个）", "55,35,23,76,60,65.1,70.2,75.3,80.4,85.5,90.6,95.7,26,76,60,65.1,70.2,75.3,80.4,95.7,26,76,60,65.1,70.2,75.3,95.7,26,76,60,65.1", "BAR"),
                                series("开工率（%）", "34,45,23,67,34,45,23,67,34,45,23,67,23,67,34,45,23,45,23,67,23,67,34,45,23,45,67,23,67,34,45", "LINE")))
        ));
        return config;
    }

    private ReportConfig.ChartConfig chart(String labelName, String title, String type,
                                           List<String> categories, List<ReportConfig.SeriesConfig> series) {
        ReportConfig.ChartConfig chart = new ReportConfig.ChartConfig();
        chart.setLabelName(labelName);
        chart.setTitle(title);
        chart.setType(type);
        chart.setCategories(categories);
        chart.setSeries(series);
        return chart;
    }

    private ReportConfig.SeriesConfig series(String name, String data, String comboType) {
        ReportConfig.SeriesConfig series = new ReportConfig.SeriesConfig();
        series.setName(name);
        List<Number> numbers = new ArrayList<>();
        for (String part : data.split(",")) {
            String value = part.trim();
            if (!value.isEmpty()) {
                numbers.add(value.contains(".") ? Double.parseDouble(value) : Long.parseLong(value));
            }
        }
        series.setData(numbers);
        series.setComboType(comboType);
        return series;
    }

    private File loadTemplate() {
        return loadTemplate(TEMPLATE_PATH);
    }

    private File loadTemplate(String path) {
        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            File temp = File.createTempFile("generate_report_template_", ".docx");
            Files.copy(in, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            temp.deleteOnExit();
            return temp;
        } catch (IOException e) {
            throw new RuntimeException("加载 Word 模板失败", e);
        }
    }

    private File decodeImageFile(String base64, String labelName) {
        if (base64 == null || base64.trim().isEmpty()) {
            throw new RuntimeException("图表图片为空：" + labelName);
        }
        String data = base64.trim();
        int comma = data.indexOf(',');
        if (data.startsWith("data:") && comma > 0) {
            data = data.substring(comma + 1);
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(data);
            File image = File.createTempFile("chart_" + labelName + "_", ".png");
            Files.write(image.toPath(), bytes);
            image.deleteOnExit();
            return image;
        } catch (Exception e) {
            throw new RuntimeException("图表图片解析失败：" + labelName, e);
        }
    }

    private List<LabelData> buildImageContents(ReportConfig config) {
        List<LabelData> generates = new ArrayList<>();
        for (LabelData data : buildContents(config)) {
            if (data.getTypeEnum() != WordContentTypeEnum.CHART) {
                generates.add(data);
            }
        }
        Map<String, String> images = config.getChartImages();
        int width = 560;
        int height = 300;
        if (config.getCharts() != null) {
            for (ReportConfig.ChartConfig chartConfig : config.getCharts()) {
                String label = chartConfig.getLabelName();
                if (label == null || !images.containsKey(label)) {
                    continue;
                }
                PictureContentData picture = new PictureContentData();
                picture.setWidth(width).setHeight(height).setPicType(PicTypeEnum.PNG)
                        .setFile(decodeImageFile(images.get(label), label))
                        .setLabelName(label).setTypeEnum(WordContentTypeEnum.PICTURE);
                generates.add(picture);
            }
        }
        return generates;
    }

    private File createPictureFile(int width, int height, String text) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(new GradientPaint(0, 0, new Color(43, 108, 176), width, height, new Color(118, 76, 200)));
        g.fillRect(0, 0, width, height);
        g.setColor(Color.WHITE);
        int fontSize = Math.max(12, Math.min(40, width / 9));
        g.setFont(new Font("SansSerif", Font.BOLD, fontSize));
        FontMetrics fm = g.getFontMetrics();
        String content = text == null ? "" : text;
        g.drawString(content, Math.max(0, (width - fm.stringWidth(content)) / 2), height / 2);
        g.dispose();
        try {
            File generated = File.createTempFile("generate_report_pic_", ".png");
            ImageIO.write(image, "png", generated);
            generated.deleteOnExit();
            return generated;
        } catch (IOException e) {
            throw new RuntimeException("生成示例图片失败", e);
        }
    }

    private Pair<STNumberFormat.Enum, String> resolveListStyle(String style) {
        if (style == null) {
            return NumbericRenderData.FMT_LOWER_ROMAN;
        }
        switch (style) {
            case "bullet":
                return NumbericRenderData.FMT_BULLET;
            case "decimal":
                return NumbericRenderData.FMT_DECIMAL;
            case "lowerLetter":
                return NumbericRenderData.FMT_LOWER_LETTER;
            case "upperLetter":
                return NumbericRenderData.FMT_UPPER_LETTER;
            case "upperRoman":
                return NumbericRenderData.FMT_UPPER_ROMAN;
            case "lowerRoman":
            default:
                return NumbericRenderData.FMT_LOWER_ROMAN;
        }
    }

    private List<LabelData> buildContents(ReportConfig config) {
        List<LabelData> generates = new ArrayList<>();
        if (config == null) {
            config = getDefaultConfig();
        }

        if (config.getTitle() != null) {
            TextContentData title = new TextContentData();
            title.setContent(config.getTitle()).setLabelName("title").setTypeEnum(WordContentTypeEnum.TEXT);
            generates.add(title);
        }

        if (config.getTypeContent() != null) {
            ReportConfig.StyledText styled = config.getTypeContent();
            TextContentData text = new TextContentData();
            String color = styled.getColor() == null || styled.getColor().isEmpty() ? "000000" : styled.getColor();
            text.setRenderData(new TextRenderData(color, styled.getText() == null ? "" : styled.getText()))
                    .setLabelName("typeContent").setTypeEnum(WordContentTypeEnum.TEXT);
            generates.add(text);
        }

        if (config.getPicture() != null) {
            ReportConfig.PictureConfig pic = config.getPicture();
            int width = pic.getWidth() == null ? 200 : pic.getWidth();
            int height = pic.getHeight() == null ? 160 : pic.getHeight();
            PictureContentData picture = new PictureContentData();
            picture.setWidth(width).setHeight(height).setPicType(PicTypeEnum.PNG);
            if (pic.getUrl() != null && !pic.getUrl().trim().isEmpty()) {
                picture.setPicUrl(pic.getUrl().trim());
            } else {
                File legacy = new File("D:\\down\\java.jpg");
                picture.setFile(legacy.isFile() ? legacy : createPictureFile(width, height, pic.getText()));
            }
            picture.setLabelName("picture").setTypeEnum(WordContentTypeEnum.PICTURE);
            generates.add(picture);
        }

        if (config.getShowTable() != null) {
            ReportConfig.TableConfig tableConfig = config.getShowTable();
            TableSeriesRenderData table = new TableSeriesRenderData();
            List<TextRenderData[]> rows = new ArrayList<>();
            if (tableConfig.getRows() != null) {
                for (List<String> row : tableConfig.getRows()) {
                    TextRenderData[] cells = new TextRenderData[row == null ? 0 : row.size()];
                    if (row != null) {
                        for (int i = 0; i < row.size(); i++) {
                            cells[i] = new TextRenderData(row.get(i) == null ? "" : row.get(i));
                        }
                    }
                    rows.add(cells);
                }
            }
            List<TextRenderData> headers = new ArrayList<>();
            if (tableConfig.getHeaders() != null) {
                for (String header : tableConfig.getHeaders()) {
                    headers.add(new TextRenderData(header == null ? "" : header));
                }
            }
            table.setHeader(headers.toArray(new TextRenderData[0]))
                    .setContents(rows).setLabelName("showTable").setTypeEnum(WordContentTypeEnum.TABLE);
            generates.add(table);
        }

        if (config.getNumList() != null) {
            ReportConfig.ListConfig listConfig = config.getNumList();
            ListRenderData list = new ListRenderData();
            List<TextRenderData> items = new ArrayList<>();
            if (listConfig.getItems() != null) {
                for (String item : listConfig.getItems()) {
                    items.add(new TextRenderData(item == null ? "" : item));
                }
            }
            list.setList(items).setPair(resolveListStyle(listConfig.getStyle()))
                    .setTypeEnum(WordContentTypeEnum.LIST).setLabelName("numList");
            generates.add(list);
        }

        if (config.getCharts() != null) {
            for (ReportConfig.ChartConfig chartConfig : config.getCharts()) {
                if (chartConfig.getLabelName() == null || chartConfig.getLabelName().trim().isEmpty()) {
                    continue;
                }
                ChartSeriesRenderData chart = new ChartSeriesRenderData();
                chart.setTitle(chartConfig.getTitle())
                        .setLabelName(chartConfig.getLabelName())
                        .setTypeEnum(WordContentTypeEnum.CHART);
                if (chartConfig.getCategories() != null) {
                    chart.setCategories(chartConfig.getCategories().toArray(new String[0]));
                }
                List<ChartSeriesRenderData.RenderData> seriesData = new ArrayList<>();
                if (chartConfig.getSeries() != null) {
                    for (ReportConfig.SeriesConfig item : chartConfig.getSeries()) {
                        ChartSeriesRenderData.RenderData renderData = new ChartSeriesRenderData.RenderData();
                        renderData.setRenderTitle(item.getName());
                        if (item.getData() != null) {
                            renderData.setData(item.getData().toArray(new Number[0]));
                        }
                        if (item.getComboType() != null && !item.getComboType().trim().isEmpty()) {
                            renderData.setComboType(SeriesRenderData.ComboType.valueOf(item.getComboType().trim().toUpperCase()));
                        }
                        seriesData.add(renderData);
                    }
                }
                chart.setSenderData(seriesData);
                if ("single".equalsIgnoreCase(chartConfig.getType())) {
                    chart.setCharType(CharCombinationType.Single);
                }
                generates.add(chart);
            }
        }
        return generates;
    }
}
