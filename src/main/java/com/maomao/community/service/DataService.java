package com.maomao.community.service;

import com.maomao.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author MaoJY
 * @create 2022-04-29 21:46
 * @Description:统计UV和DAU
 */
@Service
public class DataService {
    @Autowired
    private RedisTemplate redisTemplate;
    private static DateTimeFormatter dtf=DateTimeFormatter.ofPattern("yyyyMMdd");
    /**
    * Description:将指定ip加入uv
    * date: 2022/4/29 21:49
    * @author: MaoJY
    * @since JDK 1.8
    */
    public void recordUV(String ip){
        String redisKey = RedisKeyUtil.getPrefixUv(dtf.format(LocalDate.now()));
        redisTemplate.opsForHyperLogLog().add(redisKey,ip);
    }
    /**
    * Description:通知指定范围内的uv
    * date: 2022/4/29 21:55
    * @author: MaoJY
    * @since JDK 1.8
    */
    public long calculateUV(LocalDate startDate,LocalDate endDate){
        if(startDate==null||endDate==null){
            throw  new IllegalArgumentException("参数不能为空！");
        }
        //整理该范围内的key
        List<String> keyList=new ArrayList<>();
        LocalDate index=startDate;
        while(!index.isAfter(endDate)){
            String redisKey = RedisKeyUtil.getPrefixUv(index.format(dtf));
            keyList.add(redisKey);
           index= index.plusDays(1);
        }
        String unionKey = RedisKeyUtil.getPrefixUv(dtf.format(startDate), dtf.format(endDate));
        redisTemplate.opsForHyperLogLog().union(unionKey,keyList.toArray());
        return redisTemplate.opsForHyperLogLog().size(unionKey);
    }
    /**
    * Description:将指定用户加入dau
    * date: 2022/4/29 22:08
    * @author: MaoJY
    * @since JDK 1.8
    */
    public void recordDAU(int userId){
        String redisKey = RedisKeyUtil.getPrefixUv(dtf.format(LocalDate.now()));
        redisTemplate.opsForValue().setBit(redisKey,userId,true);
    }
    /**
    * Description:通知指定日期范围内的dau
    * date: 2022/4/29 22:10
    * @author: MaoJY
    * @since JDK 1.8
    */
    public long calculateDAU(LocalDate startDate,LocalDate endDate){
        if(startDate==null||endDate==null){
            throw  new IllegalArgumentException("参数不能为空！");
        }
        //整理该范围内的key
        List<byte[]> keyList=new ArrayList<>();
        LocalDate index=startDate;
        while(!index.isAfter(endDate)){
            String redisKey = RedisKeyUtil.getPrefixUv(index.format(dtf));
            keyList.add(redisKey.getBytes());
          index= index.plusDays(1);
        }
       return (long)redisTemplate.execute(new RedisCallback() {
           @Override
           public Object doInRedis(RedisConnection connection) throws DataAccessException {
               String unionKey = RedisKeyUtil.getPrefixDau(startDate.format(dtf), endDate.format(dtf));
               connection.bitOp(RedisStringCommands.BitOperation.OR,unionKey.getBytes(),keyList.toArray(new byte[0][0]));
               return connection.bitCount(unionKey.getBytes());
           }
       });
    }

}