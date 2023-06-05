package com.jiashn.generateReport.domain;

import com.jiashn.generateReport.enums.PicTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.File;

/**
 * @author: jiangjs
 * @description: 图片
 * @date: 2022/11/24 16:34
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class PictureContentData extends LabelData {
    /**
     * 图片宽度
     */
    private Integer width;
    /**
     * 图片高度
     */
    private Integer height;
    /**
     * 图片类型
     */
    private PicTypeEnum picType;
    /**
     * 图片地址（网络图片插入时使用）
     */
    private String picUrl;
    /**
     * 图片文件
     */
    private File file;
}