package com.maomao.community.util;

/**
 * @author MaoJY
 * @create 2022-04-14 21:45
 * @Description:生成同一格式的redisKey
 */
public class RedisKeyUtil {
    private static  final String SPLIT=":";
    private static final String PREFIX_ENTITY_LIKE ="like:entity";
    private static final String PREFIX_USER_LIKE ="like:user";

    public static  String getPrefixEntityLike(int entityType,int entityId){
        return PREFIX_ENTITY_LIKE+SPLIT+entityType+SPLIT+entityId;
    }
    public static  String getPrefixUserLike(int userId){
        return  PREFIX_USER_LIKE+SPLIT+userId;
    }
}