package com.maomao.community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.maomao.community.dao.DiscussPostMapper;
import com.maomao.community.entity.DiscussPost;
import com.maomao.community.filter.SensitiveWordFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private SensitiveWordFilter sensitiveWordFilter;
    @Value("${caffeine.posts.max-size}")
    private int maxSize;
    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;
    //缓存帖子列表
    private LoadingCache<String,List<DiscussPost>> postListCache;
    //缓存帖子行数
    private LoadingCache<Integer,Integer> postRowCache;
    //初始化
    @PostConstruct
    public void init(){
        //初始化列表缓存
        postListCache= Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Override
                    public List<DiscussPost> load(String key) throws Exception {
                        if (key == null||key.length()==0) {
                            throw new IllegalArgumentException("参数错误");
                        }
                        String[] split = key.split(":");
                        if (split==null||split.length!=2){
                            throw new IllegalArgumentException("参数错误");
                        }
                        Integer offset=Integer.valueOf(split[0]);
                        Integer limit=Integer.valueOf(split[1]);
                        //这里可以先查2级缓存
                        return discussPostMapper.selectDiscussPosts(0,offset,limit,1);
                    }
                });
        //初始化行数
        postRowCache=Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds,TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public Integer load(Integer key) throws Exception {
                        if (key == null) {
                            throw new IllegalArgumentException("参数错误");
                        }
                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });
    }
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit,int orderMode) {
//        未登录，并且查看热门帖子时，才缓存
        if(userId==0&&orderMode==1){
            return  postListCache.get(offset+":"+limit);
        }
        log.debug("load post list from DB");
        return discussPostMapper.selectDiscussPosts(userId, offset, limit,orderMode);
    }

    public int findDiscussPostRows(int userId) {
        if (userId==0) {
            return postRowCache.get(userId);
        }
        log.debug("load post list from DB");
        return discussPostMapper.selectDiscussPostRows(userId);
    }
    /**
    * Description:插入评论
    * date: 2022/4/5 21:52
    * @author: MaoJY
    * @since JDK 1.8
    */
    public int insertDiscussPost(DiscussPost post){
        if (post == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        post.setTitle(sensitiveWordFilter.filter(post.getTitle()));
        post.setContent(sensitiveWordFilter.filter(post.getContent()));
        return discussPostMapper.insertDiscussPost(post);
    }
    /**
    * Description:帖子详情
    * date: 2022/4/6 16:08
    * @author: MaoJY
    * @since JDK 1.8
    */
    public DiscussPost discussPostDetail(int id){
      return  discussPostMapper.discussPostDetail(id);
    }
    /**
    * Description:修改帖子数
    * date: 2022/4/6 18:48
    * @author: MaoJY
    * @since JDK 1.8
    */
    public int updateCommentCount(int id,int commentCount){
        return discussPostMapper.updateCommentCount(id,commentCount);
    }

    public DiscussPost findDiscussById(int id){
        return  discussPostMapper.findDiscussById(id);
    }
    public void updateType(int id,int type){
        discussPostMapper.updateType(id, type);
    }
    public void updateStatus (int id,int status){
        discussPostMapper.updateStatus(id, status);
    }
    /**
    * Description:修改帖子分数
    * date: 2022/5/1 19:10
    * @author: MaoJY
    * @since JDK 1.8
    */
    public void updateScore(int postId, double score) {
        discussPostMapper.updateScore(postId,score);
    }
}
