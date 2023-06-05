package com.jiashn.generateReport.enums;

/**
 * @author: jiangjs
 * @description:
 * @date: 2023/6/2 16:51
 **/
public enum CharCombinationType {

    /**
     * 多组合
     */
    MULTI("Multi"),
    /**
     * 单图形
     */
    Single("Single");

    private final String type;

    CharCombinationType(String type){
        this.type = type;
    }
    public String getType(){
        return type;
    }
}
