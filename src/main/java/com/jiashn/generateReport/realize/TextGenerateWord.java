package com.jiashn.generateReport.realize;

import com.jiashn.generateReport.domain.LabelData;
import com.jiashn.generateReport.domain.TextContentData;
import com.jiashn.generateReport.enums.WordContentTypeEnum;
import com.jiashn.generateReport.factory.GenerateWordFactory;
import com.jiashn.generateReport.service.GenerateWord;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Objects;

/**
 * @author: jiangjs
 * @description: 文本内容实现
 * @date: 2022/11/24 14:28
 **/
@Component
public class TextGenerateWord implements GenerateWord {

    @PostConstruct
    public void init(){
        GenerateWordFactory.register(WordContentTypeEnum.TEXT,this);
    }

    @Override
    public Object generateWord(LabelData data) {
        TextContentData contentData = (TextContentData) data;
        return Objects.nonNull(contentData.getLinkData()) ? contentData.getLinkData() :
                Objects.nonNull(contentData.getRenderData()) ? contentData.getRenderData() : contentData.getContent();
    }
}