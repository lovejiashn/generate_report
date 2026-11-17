package com.jiashn.generateReport.util;

import com.deepoove.poi.XWPFTemplate;
import com.jiashn.generateReport.domain.LabelData;
import com.jiashn.generateReport.factory.GenerateWordFactory;
import com.jiashn.generateReport.service.GenerateWord;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.util.*;

/**
 * @author: jiangjs
 * @description: 操作word内容
 * @date: 2022/11/24 11:32
 **/
public class OperateWordManage {
    private final static Logger log = LoggerFactory.getLogger(OperateWordManage.class);

    public static byte[] generateWordBytes(File templateFile, List<LabelData> contents){
        XWPFTemplate template = null;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            template = XWPFTemplate.compile(templateFile).render(buildData(contents));
            applyTableBorders(template);
            template.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            log.error("生成 Word 报表报错：{}", e.getMessage());
            throw new RuntimeException("生成 Word 报表失败", e);
        } finally {
            if (Objects.nonNull(template)) {
                try {
                    template.close();
                } catch (Exception e) {
                    log.error("关闭模板报错：{}", e.getMessage());
                }
            }
        }
    }

    private static void applyTableBorders(XWPFTemplate template) {
        for (XWPFTable table : template.getXWPFDocument().getTables()) {
            CTTblBorders borders = table.getCTTbl().getTblPr().isSetTblBorders()
                    ? table.getCTTbl().getTblPr().getTblBorders()
                    : table.getCTTbl().getTblPr().addNewTblBorders();
            setBorder(borders.isSetTop() ? borders.getTop() : borders.addNewTop());
            setBorder(borders.isSetLeft() ? borders.getLeft() : borders.addNewLeft());
            setBorder(borders.isSetBottom() ? borders.getBottom() : borders.addNewBottom());
            setBorder(borders.isSetRight() ? borders.getRight() : borders.addNewRight());
            setBorder(borders.isSetInsideH() ? borders.getInsideH() : borders.addNewInsideH());
            setBorder(borders.isSetInsideV() ? borders.getInsideV() : borders.addNewInsideV());
        }
    }

    private static void setBorder(CTBorder border) {
        border.setVal(STBorder.SINGLE);
        border.setSz(BigInteger.valueOf(8));
        border.setSpace(BigInteger.ZERO);
        border.setColor("000000");
    }

    public static void generateWordContent(File tempFileFile, String destFilePath, List<LabelData> contents){
        byte[] bytes = generateWordBytes(tempFileFile, contents);
        try (FileOutputStream fos = new FileOutputStream(destFilePath)) {
            fos.write(bytes);
            fos.flush();
        } catch (Exception e) {
            log.error("写入 Word 文件报错：{}", e.getMessage());
            throw new RuntimeException("写入 Word 文件失败", e);
        }
    }

    private static Map<String, Object> buildData(List<LabelData> contents) {
        Map<String, Object> data = new HashMap<>(contents.size());
        contents.forEach(content -> {
            GenerateWord backData = GenerateWordFactory.getBackData(content.getTypeEnum());
            data.put(content.getLabelName(), backData.generateWord(content));
        });
        return data;
    }
}
