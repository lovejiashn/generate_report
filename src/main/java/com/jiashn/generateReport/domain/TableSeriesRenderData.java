package com.jiashn.generateReport.domain;

import com.deepoove.poi.data.TextRenderData;
import com.jiashn.generateReport.domain.LabelData;
import com.jiashn.generateReport.domain.LabelData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author: jiangjs
 * @description:
 * @date: 2022/11/24 17:19
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class TableSeriesRenderData extends LabelData {

    /**
     * 表头
     */
    private TextRenderData[] header;
    /**
     * 表内容
     */
    private List<TextRenderData[]> contents;
}