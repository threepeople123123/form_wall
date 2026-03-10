package com.wj.future.compus.campusEnum;

/**
 * packageName com.wj.future.compus.campusEnum
 *
 * @author wj
 * @className RedisEnum
 * @date 2026/3/10
 * @description redis美剧
 */
public enum RedisEnum {
    //构造枚举
    USER_BOT_TO_CONVERSATION("future:campus:user:to:ai");

    private final String key;

    RedisEnum(String key){
        this.key = key;
    }

    public String getKey(){
        return key;
    }
}
