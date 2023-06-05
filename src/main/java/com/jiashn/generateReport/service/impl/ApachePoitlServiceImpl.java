package com.jiashn.generateReport.service.impl;
import com.deepoove.poi.data.ChartSingleSeriesRenderData;
import com.deepoove.poi.data.NumbericRenderData;
import com.deepoove.poi.data.SeriesRenderData;
import com.deepoove.poi.data.TextRenderData;
import com.deepoove.poi.data.style.Style;
import com.jiashn.generateReport.domain.*;
import com.jiashn.generateReport.enums.CharCombinationType;
import com.jiashn.generateReport.enums.PicTypeEnum;
import com.jiashn.generateReport.enums.WordContentTypeEnum;
import com.jiashn.generateReport.realize.TableGenerateWord;
import com.jiashn.generateReport.service.ApachePoitlService;
import com.jiashn.generateReport.util.OperateWordManage;
import com.sun.org.apache.bcel.internal.generic.NEW;
import javafx.application.Application;
import org.apache.catalina.core.ApplicationContext;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author: jiangjs
 * @description:
 * @date: 2022/11/23 17:13
 **/
@Service
public class ApachePoitlServiceImpl implements ApachePoitlService {

    private static final String TEMPLATE_PATH = "static/template/demo_template.docx";

    @Override
    public void generateCharts() {
        File templateFile = null;
        try {
            templateFile = new ClassPathResource(TEMPLATE_PATH).getFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<LabelData> generates = new ArrayList<>();
        //文本
        TextContentData contentData = new TextContentData();
        contentData.setContent("2022年月通报函生成报告").setLabelName("title").setTypeEnum(WordContentTypeEnum.TEXT);
        generates.add(contentData);
        //带样式文本
        TextContentData typeData = new TextContentData();
        typeData.setRenderData(new TextRenderData("cc0000","这是带样式的内容")).setLabelName("typeContent").setTypeEnum(WordContentTypeEnum.TEXT);
        generates.add(typeData);
        //插入图片
        PictureContentData picData = new PictureContentData();
        picData.setWidth(200).setHeight(160).setPicType(PicTypeEnum.JPG).setFile(new File("D:\\down\\java.jpg"))
                .setLabelName("picture").setTypeEnum(WordContentTypeEnum.PICTURE);
        generates.add(picData);
        //插入表格
        TableSeriesRenderData tableData = new TableSeriesRenderData();
        List<TextRenderData[]> contents = Arrays.asList(new TextRenderData[]{new TextRenderData("科教1班"),
                new TextRenderData("1")},new TextRenderData[]{new TextRenderData("幼儿3班"),new TextRenderData("6")});
        tableData.setHeader(new TextRenderData[]{new TextRenderData("班级"),new TextRenderData("排名")})
                .setContents(contents).setLabelName("showTable").setTypeEnum(WordContentTypeEnum.TABLE);
        generates.add(tableData);
        //插入列表
        ListRenderData listRenderData = new ListRenderData();
        List<TextRenderData> listData = Arrays.asList(new TextRenderData("排序1"),new TextRenderData("排序2"),new TextRenderData("排序3"));
        listRenderData.setList(listData).setPair(NumbericRenderData.FMT_LOWER_ROMAN).setTypeEnum(WordContentTypeEnum.LIST).setLabelName("numList");
        generates.add(listRenderData);
        //折线
        ChartSeriesRenderData lineData = new ChartSeriesRenderData();
        List<ChartSeriesRenderData.RenderData> lineRenderData = new ArrayList<>();
        ChartSeriesRenderData.RenderData numRenderData = new ChartSeriesRenderData.RenderData();
        ChartSeriesRenderData.RenderData moneyRenderData = new ChartSeriesRenderData.RenderData();
        numRenderData.setRenderTitle("项目数量").setData(new Double[] {-11.02,-19.42,-10.61,-11.41,-7.91,-5.44,-5.30,-2.75,-1.24,0.35});
        moneyRenderData.setRenderTitle("投资额").setData(new Number[]{-12.66,-19.41,-15.16,-19.72,-17.05,-15.92,-15.10,-13.04,-10.65,-9.15});
        lineRenderData.add(numRenderData);
        lineRenderData.add(moneyRenderData);
        lineData.setTitle("1-10月份全国新开工项目数量、投资额增速")
                .setCategories(new String[] {"1月","2月","3月","4月","5月","6月","7月","8月","9月","10月"})
                .setSenderData(lineRenderData).setTypeEnum(WordContentTypeEnum.CHART).setLabelName("speedLine");
        generates.add(lineData);
        //柱状图
        ChartSeriesRenderData barData = new ChartSeriesRenderData();
        List<ChartSeriesRenderData.RenderData> barRenderData = new ArrayList<>();
        ChartSeriesRenderData.RenderData openRenderData = new ChartSeriesRenderData.RenderData();
        ChartSeriesRenderData.RenderData moneyData = new ChartSeriesRenderData.RenderData();
        openRenderData.setRenderTitle("开工数量").setData(new Number[]{40,50,45,12,21,18,21,28,21,18,28,18,20,19,-10,-9,-10,19,39,31,20,19,-10,-9,-10,19,39,31,-10,19,39});
        moneyData.setRenderTitle("投资额").setData(new Number[]{20,-22,-12,8,-10,-14,-10,-10,-8,-2,-8,-1,-9,-21,-9,-7,-21,-10,21,-29,-50,-21,-9,-7,-21,-10,21,-29,-21,-10,21});
        barRenderData.add(openRenderData);
        barRenderData.add(moneyData);
        barData.setTitle("各省（自治区）直辖市新开项目数量、投资额同比情况")
                .setCategories(new String[] {"贵州","西藏","黑龙江","浙江","湖北","江苏","四川","福建","安徽","海南","山西","广西","青海","广东","甘肃",
                        "云南","宁夏","新疆","湖南","北京","河北","山西","山东","内蒙古","天津","江西","吉林","河南","重庆","上海","辽宁"})
                .setSenderData(barRenderData).setTypeEnum(WordContentTypeEnum.CHART).setLabelName("investmentRatio");
        generates.add(barData);
        //生成饼图
        ChartSeriesRenderData areaData = new ChartSeriesRenderData();
        List<ChartSeriesRenderData.RenderData> areaRenderDatas = new ArrayList<>();
        ChartSeriesRenderData.RenderData areaRenderData = new ChartSeriesRenderData.RenderData();
        areaRenderData.setData(new Number[]{17098242, 9984670, 9826675, 9596961}).setRenderTitle("投资额")
                .setComboType(SeriesRenderData.ComboType.AREA);
        areaRenderDatas.add(areaRenderData);
        areaData.setTitle("国家投资额").setSenderData(areaRenderDatas).setCharType(CharCombinationType.Single)
                .setCategories(new String[]{"俄罗斯", "加拿大", "美国", "中国"})
                .setLabelName("areaShow").setTypeEnum(WordContentTypeEnum.CHART);
        generates.add(areaData);

        //横向柱状图
        ChartSeriesRenderData lateralData = new ChartSeriesRenderData();
        List<ChartSeriesRenderData.RenderData> lateralRenderData = new ArrayList<>();
        ChartSeriesRenderData.RenderData lateralYearData = new ChartSeriesRenderData.RenderData();
        ChartSeriesRenderData.RenderData lateralMoneyData = new ChartSeriesRenderData.RenderData();
        lateralYearData.setRenderTitle("2021年").setData(new Number[]{400,200});
        lateralMoneyData.setRenderTitle("2022年").setData(new Number[]{456,255});
        lateralRenderData.add(lateralYearData);
        lateralRenderData.add(lateralMoneyData);
        lateralData.setTitle("工程建设项目建设周期同比情况")
                .setCategories(new String[] {"从立项到开工的用时","从开工到验收的用时"})
                .setSenderData(lateralRenderData).setTypeEnum(WordContentTypeEnum.CHART).setLabelName("cycleRadio");
        generates.add(lateralData);
        //组合图表
        ChartSeriesRenderData groupData = new ChartSeriesRenderData();
        List<ChartSeriesRenderData.RenderData> groupRenderData = new ArrayList<>();
        ChartSeriesRenderData.RenderData unOpenData = new ChartSeriesRenderData.RenderData();
        ChartSeriesRenderData.RenderData openRadioData = new ChartSeriesRenderData.RenderData();
        unOpenData.setComboType(SeriesRenderData.ComboType.BAR).setRenderTitle("未开工项目数（个）")
                .setData(new Number[]{55, 35, 23, 76, 60, 65.1, 70.2, 75.3, 80.4, 85.5, 90.6, 95.7, 26,
                76, 60, 65.1, 70.2, 75.3, 80.4, 95.7, 26, 76, 60, 65.1, 70.2, 75.3, 95.7, 26, 76, 60, 65.1});
        openRadioData.setComboType(SeriesRenderData.ComboType.LINE).setRenderTitle("开工率（%）")
                .setData(new Number[]{34,45,23,67,34,45,23,67,34,45,23,67,23,67,34,45,23,45,23,67,23,67,34,45,23,45,67,23,67,34,45});
        groupRenderData.add(unOpenData);
        groupRenderData.add(openRadioData);
        groupData.setTitle("各省（区、市）签约项目开工情况")
                .setCategories(new String[] {"北京","吉林","云南","上海","安徽","浙江","江西","四川","陕西","甘肃","江苏","广西","内蒙古","福建","天津","海南","黑龙江",
                        "贵州","山东","河北","辽宁","湖北","宁夏","广东","重庆","河南","新疆","山西","湖南","青海","兵团"})
                .setSenderData(groupRenderData).setTypeEnum(WordContentTypeEnum.CHART).setLabelName("openCondition");
        generates.add(groupData);
        //生成word
        OperateWordManage.generateWordContent(templateFile,"D:\\down\\output.docx",generates);
    }
}