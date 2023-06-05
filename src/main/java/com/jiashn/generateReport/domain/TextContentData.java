package com.jiashn.generateReport.domain;

import com.deepoove.poi.data.HyperLinkTextRenderData;
import com.deepoove.poi.data.TextRenderData;
import com.jiashn.generateReport.domain.LabelData;
import com.jiashn.generateReport.domain.LabelData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author: jiangjs
 * @description: 文本
 * @date: 2022/11/24 15:07
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class TextContentData extends LabelData {

    /**
     * 纯文本内容
     */
    private String content;
    /**
     * 带样式文本
     */
    private TextRenderData renderData;
    /**
     * 超链接文本
     */
    private HyperLinkTextRenderData linkData;
}