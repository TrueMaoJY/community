package com.maomao.community.service;

import com.maomao.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * @author MaoJY
 * @create 2022-04-14 21:54
 * @Description:点赞功能service层
 */
@Service
public class LikesService {
    @Autowired
    RedisTemplate redisTemplate;
    /**
    * Description:点赞或者取消点赞
    * date: 2022/4/14 21:55
    * @author: MaoJY
    * @since JDK 1.8
    */
    public void likes(int entityType,int entityId,int userId,int entityUserId){
//        String key = RedisKeyUtil.getPrefixEntityLike(entityType, entityId);
//        Boolean member = redisTemplate.opsForSet().isMember(key, userId);
//        if (member) {
//            redisTemplate.opsForSet().remove(key,userId);
//        }else {
//            redisTemplate.opsForSet().add(key,userId);
//        }
        //两步更新操作需要开始事务
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String userLikeKey = RedisKeyUtil.getPrefixUserLike(entityUserId);
                String entityLikeKey = RedisKeyUtil.getPrefixEntityLike(entityType,entityId);
                Boolean member = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
                redisOperations.multi();
                if (member) {
                    redisOperations.opsForSet().remove(entityLikeKey,userId);
                    redisOperations.opsForValue().decrement(userLikeKey);
                }else {
                    redisOperations.opsForSet().add(entityLikeKey,userId);
                    redisOperations.opsForValue().increment(userLikeKey);
                }
                return redisOperations.exec();
            }
        });
    }
    /**
    * Description:记录点赞的数量
    * date: 2022/4/14 21:58
    * @author: MaoJY
    * @since JDK 1.8
    */
    public long getLikeCount(int entityType,int entityId){
        String key = RedisKeyUtil.getPrefixEntityLike(entityType, entityId);
       return  redisTemplate.opsForSet().size(key);
    }
    /**
    * Description:记录点赞与否的状态
    * date: 2022/4/14 22:00
    * @author: MaoJY
    * @since JDK 1.8
    */
    public int findEntityLikeStatus(int userId,int entityType,int entityId){
        String key = RedisKeyUtil.getPrefixEntityLike(entityType, entityId);
        return redisTemplate.opsForSet().isMember(key,userId)?1:0;
    }

    /**
    * Description:统计用户被赞的数量
    * date: 2022/4/15 22:54
    * @author: MaoJY
    * @since JDK 1.8
    */
    public  int getUserLikeCount(int userId){
        String userLikeKey = RedisKeyUtil.getPrefixUserLike(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return  count==null?0:count.intValue();
    }
}