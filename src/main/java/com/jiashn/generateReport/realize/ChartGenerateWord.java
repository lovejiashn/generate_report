package com.jiashn.generateReport.realize;

import com.deepoove.poi.data.ChartMultiSeriesRenderData;
import com.deepoove.poi.data.ChartSingleSeriesRenderData;
import com.deepoove.poi.data.SeriesRenderData;
import com.jiashn.generateReport.domain.ChartSeriesRenderData;
import com.jiashn.generateReport.domain.LabelData;
import com.jiashn.generateReport.enums.WordContentTypeEnum;
import com.jiashn.generateReport.factory.GenerateWordFactory;
import com.jiashn.generateReport.service.GenerateWord;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author: jiangjs
 * @description: 图表类型
 * @date: 2022/11/24 14:32
 **/
@Component
public class ChartGenerateWord implements GenerateWord {
    @PostConstruct
    private void init(){
        GenerateWordFactory.register(WordContentTypeEnum.CHART,this);
    }
    @Override
    public Object generateWord(LabelData obj) {
        ChartSeriesRenderData renderData  = (ChartSeriesRenderData) obj;
        if (Objects.nonNull(renderData.getCharType()) && Objects.equals("Single",renderData.getCharType().getType())){
            ChartSingleSeriesRenderData singleSeriesRenderData = new ChartSingleSeriesRenderData();
            singleSeriesRenderData.setCategories(renderData.getCategories());
            singleSeriesRenderData.setChartTitle(renderData.getTitle());
            ChartSeriesRenderData.RenderData seriesData = renderData.getSenderData().get(0);
            SeriesRenderData srd = new SeriesRenderData(seriesData.getRenderTitle(),seriesData.getData());
            if (Objects.nonNull(seriesData.getComboType())){
                srd.setComboType(seriesData.getComboType());
            }
            singleSeriesRenderData.setSeriesData(srd);
            return singleSeriesRenderData;
        } else {
            ChartMultiSeriesRenderData seriesRenderData = new ChartMultiSeriesRenderData();
            seriesRenderData.setCategories(renderData.getCategories());
            seriesRenderData.setChartTitle(renderData.getTitle());
            List<ChartSeriesRenderData.RenderData> renderDataList = renderData.getSenderData();
            List<SeriesRenderData> groupData = new ArrayList<>();
            renderDataList.forEach(data -> {
                SeriesRenderData srd = new SeriesRenderData(data.getRenderTitle(),data.getData());
                if (Objects.nonNull(data.getComboType())){
                    srd.setComboType(data.getComboType());
                }
                groupData.add(srd);
            });
            seriesRenderData.setSeriesDatas(groupData);
            return seriesRenderData;
        }
    }
}