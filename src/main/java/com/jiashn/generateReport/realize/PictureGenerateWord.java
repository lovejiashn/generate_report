package com.jiashn.generateReport.realize;

import com.deepoove.poi.data.PictureRenderData;
import com.deepoove.poi.util.BytePictureUtils;
import com.jiashn.generateReport.domain.LabelData;
import com.jiashn.generateReport.domain.PictureContentData;
import com.jiashn.generateReport.enums.WordContentTypeEnum;
import com.jiashn.generateReport.factory.GenerateWordFactory;
import com.jiashn.generateReport.service.GenerateWord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author: jiangjs
 * @description:
 * @date: 2022/11/24 16:46
 **/
@Component
public class PictureGenerateWord implements GenerateWord {

    @PostConstruct
    private void init(){
        GenerateWordFactory.register(WordContentTypeEnum.PICTURE,this);
    }

    @Override
    public Object generateWord(LabelData data) {
        PictureContentData picture = (PictureContentData) data;
        return StringUtils.isNotBlank(picture.getPicUrl()) ? new PictureRenderData(picture.getWidth(),picture.getHeight(),picture.getPicType().getPicName(),
                BytePictureUtils.getUrlBufferedImage(picture.getPicUrl()))
                : new PictureRenderData(picture.getWidth(),picture.getHeight(),picture.getFile());
    }
}