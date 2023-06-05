package com.jiashn.generateReport.domain;

import com.deepoove.poi.data.NumbericRenderData;
import com.deepoove.poi.data.TextRenderData;
import com.jiashn.generateReport.domain.LabelData;
import com.jiashn.generateReport.domain.LabelData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.tuple.Pair;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STNumberFormat;

import java.util.List;

/**
 * @author: jiangjs
 * @description: 列表
 * @date: 2023/6/2 15:46
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ListRenderData extends LabelData {
    /**
     * 列表数据集
     */
    private List<TextRenderData> list;

    /**
     * 列表样式,支持罗马字符、有序无序等,默认为点
     */
    private Pair<STNumberFormat.Enum, String> pair = NumbericRenderData.FMT_BULLET;
}
