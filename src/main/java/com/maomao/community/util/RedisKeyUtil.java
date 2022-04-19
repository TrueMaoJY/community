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
    private static final String FOLLOWEE="followee";
    private static final String FOLLOWER="follower";
    private static final String KAPTCHA="kaptcha";
    private static final  String LOGIN_TICKET="ticket";
    private static final String PREFIX_USER_LOGIN="user";

    public static  String getPrefixEntityLike(int entityType,int entityId){
        return PREFIX_ENTITY_LIKE+SPLIT+entityType+SPLIT+entityId;
    }
    public static  String getPrefixUserLike(int userId){
        return  PREFIX_USER_LIKE+SPLIT+userId;
    }

    public static String getfolloweeKey(int userId,int entityType){
    return FOLLOWEE+SPLIT+userId+SPLIT+entityType;
    }
    public static  String getfollowerKey(int entityType,int entityId){
        return FOLLOWER+SPLIT+entityId+SPLIT+entityId;
    }
    public static String getKaptcha(String owner){
        return KAPTCHA+SPLIT+owner;
    }
    public static String getLoginTicket(String ticket){
        return  LOGIN_TICKET+SPLIT+ticket;
    }
    public static String getPrefixUser(int userId){
        return PREFIX_USER_LOGIN+SPLIT+userId;
    }
}