package com.jiashn.generateReport.factory;

import com.jiashn.generateReport.enums.WordContentTypeEnum;
import com.jiashn.generateReport.service.GenerateWord;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author: jiangjs
 * @description: 生成word工厂
 * @date: 2022/11/24 14:06
 **/
public class GenerateWordFactory {

    private static final Map<WordContentTypeEnum, GenerateWord> TYPE_BACK_DATA = new HashMap<>();

    public static void register(WordContentTypeEnum typeEnum, GenerateWord word){
        if (Objects.nonNull(typeEnum)){
            TYPE_BACK_DATA.put(typeEnum,word);
        }
    }

    public static GenerateWord getBackData(WordContentTypeEnum typeEnum){
        return TYPE_BACK_DATA.get(typeEnum);
    }
}