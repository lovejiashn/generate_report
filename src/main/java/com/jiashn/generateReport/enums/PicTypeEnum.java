package com.jiashn.generateReport.enums;

/**
 * @author: jiangjs
 * @description: 图片类型
 * @date: 2022/11/24 16:50
 **/
public enum PicTypeEnum {
    /**
     * png图片
     */
    PNG(".png"),
    /**
     * JPG图片
     */
    JPG(".jpg"),
    /**
     * jpeg
     */
    JPEG(".jpeg");

    private final String picName;
    
    PicTypeEnum(String picName) {
        this.picName = picName;
    }

    public String getPicName() {
        return picName;
    }
    
}
