package com.jiashn.generateReport.util;

import com.deepoove.poi.XWPFTemplate;
import com.jiashn.generateReport.domain.LabelData;
import com.jiashn.generateReport.factory.GenerateWordFactory;
import com.jiashn.generateReport.service.GenerateWord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

/**
 * @author: jiangjs
 * @description: 操作word内容
 * @date: 2022/11/24 11:32
 **/
public class OperateWordManage {
    private final static Logger log = LoggerFactory.getLogger(OperateWordManage.class);

    public static void generateWordContent(File tempFileFile, String destFilePath, List<LabelData> contents){
        FileOutputStream fos = null;
        XWPFTemplate template = null;
        try {
            template = XWPFTemplate.compile(tempFileFile).render(new HashMap<String,Object>(contents.size()){{
                contents.forEach(content ->{
                    GenerateWord backData = GenerateWordFactory.getBackData(content.getTypeEnum());
                    put(content.getLabelName(),backData.generateWord(content));
                });
            }});
            fos = new FileOutputStream(destFilePath);
            template.write(fos);
            fos.flush();
        }catch (Exception e){
            log.error("替换生成图表报错：{}",e.getMessage());
            e.printStackTrace();
        }finally {
            try{
                if (Objects.nonNull(fos)){
                    fos.close();
                }
                if (Objects.nonNull(template)){
                    template.close();
                }
            }catch (Exception e){
                log.error("关闭数据流报错：{}",e.getMessage());
                e.printStackTrace();
            }
        }
    }
}