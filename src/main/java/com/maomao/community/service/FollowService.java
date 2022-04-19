package com.maomao.community.service;

import com.maomao.community.entity.User;
import com.maomao.community.util.RedisKeyUtil;
import com.maomao.community.vo.ConstantVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author MaoJY
 * @create 2022-04-16 22:35
 * @Description:点击关注，关注方根据关注的用户及实体类型生成key，存储实体id
 * 被关注方，根据实体类型和实体id生成key，存储关注方的id
 */
@Service
public class FollowService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserService userService;
    /**
    * Description：关注
    * date: 2022/4/16 22:40
    * @author: MaoJY
    * @since JDK 1.8
    */
    public void follow(int userId,int entityType,int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getfolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getfollowerKey(entityType, entityId);
                redisOperations.multi();
                redisOperations.opsForZSet().add(followeeKey,entityId,new Date().getTime());
                redisOperations.opsForZSet().add(followerKey,userId,new Date().getTime());
                return redisOperations.exec();
            }
        });
    }
    /**
    * Description:取关
    * date: 2022/4/16 22:40
    * @author: MaoJY
    * @since JDK 1.8
    */
    public void unfollow(int userId,int entityType,int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getfolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getfollowerKey(entityType, entityId);
                redisOperations.multi();
                redisOperations.opsForZSet().remove(followeeKey,entityId);
                redisOperations.opsForZSet().remove(followerKey,userId);
                return redisOperations.exec();
            }
        });
    }
    /**
    * Description:查询某个用户关注的人
    * date: 2022/4/18 16:48
    * @author: MaoJY
    * @since JDK 1.8
    */
    public List<Map<String,Object>> findfollowee(int userId,int offset,int limit){
        String followeeKey = RedisKeyUtil.getfolloweeKey(userId, ConstantVO.ENTITY_TYPE_USER);
        Set<Integer> followees = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
        if (followees == null) {
            return null;
        }
        List<Map<String,Object>> followeesList=new ArrayList<>();
        for (Integer entityId : followees) {
            Map<String,Object> map=new HashMap<>();
            //存储用户以及关注时间
            User user = userService.findUserById(entityId);
            map.put("user",user);
            Double score = redisTemplate.opsForZSet().score(followeeKey, entityId);
            map.put("followeeTime",new Date(score.longValue()));
            followeesList.add(map);
        }
        return followeesList;
    }
    /**
    * Description:查询某个用户的粉丝
    * date: 2022/4/18 18:02
    * @author: MaoJY
    * @since JDK 1.8
    */
    public List<Map<String,Object>> findfollower(int userId,int offset,int limit){
        String followerKey = RedisKeyUtil.getfollowerKey(ConstantVO.ENTITY_TYPE_USER, userId);
        Set<Integer> followers = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        if (followers == null) {
            return null;
        }
        List<Map<String,Object>> followersList=new ArrayList<>();
        for (Integer id : followers) {
            Map<String,Object> map=new HashMap<>();
            User user = userService.findUserById(id);
            map.put("user",user);
            Double score = redisTemplate.opsForZSet().score(followerKey, id);
            map.put("followerTime",score.longValue());
            followersList.add(map);
        }
        return followersList;
    }
    /**
    * Description:查询关注的实体的数量
    * date: 2022/4/18 18:33
    * @author: MaoJY
    * @since JDK 1.8
    */
    public long getFolloweesCount(int userId,int entityType){
        String followeeKey = RedisKeyUtil.getfolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }
    /**
    * Description:查询粉丝数量
    * date: 2022/4/18 18:35
    * @author: MaoJY
    * @since JDK 1.8
    */
    public long getFollowersCount(int entityType,int entityId){
        String followerKey = RedisKeyUtil.getfollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }
    /**
    * Description:查询当前用户是否已经关注该实体
    * date: 2022/4/18 18:35
    * @author: MaoJY
    * @since JDK 1.8
    */
    public boolean hasFollow(int userId,int entityType,int entityId){
        String followeeKey = RedisKeyUtil.getfolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey,entityId)!=null;
    }

}