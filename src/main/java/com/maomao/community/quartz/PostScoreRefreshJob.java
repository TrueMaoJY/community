package com.maomao.community.quartz;

import com.maomao.community.entity.DiscussPost;
import com.maomao.community.service.DiscussPostService;
import com.maomao.community.service.ElasticsearchService;
import com.maomao.community.service.LikesService;
import com.maomao.community.util.RedisKeyUtil;
import com.maomao.community.vo.ConstantVO;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;


import java.time.LocalDateTime;
import java.time.ZoneOffset;


/**
 * @author MaoJY
 * @create 2022-05-01 18:37
 * @Description:刷新帖子分数
 */
@Slf4j
public class PostScoreRefreshJob implements Job {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private LikesService likesService;
    @Autowired
    private ElasticsearchService elasticsearchService;
    private static final LocalDateTime epoch;
    static {
        epoch= LocalDateTime.parse("2014-08-01T00:00:00");
    }
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey= RedisKeyUtil.getPrefixPost();
        BoundSetOperations ops = redisTemplate.boundSetOps(redisKey);
        if (ops.size()==0){
            log.info("[任务取消]，没有需要刷新的帖子");
            return;
        }
        log.info("[任务开始]正在刷新帖子分数："+ops.size());
        while (ops.size()>0){
            this.refresh((Integer)ops.pop());
        }
        log.info("[任务结束],帖子分数刷新完毕");

    }

    private void refresh(int postId) {
        DiscussPost post = discussPostService.findDiscussById(postId);
        if (post == null) {
            log.error("该帖子不存在：id="+postId);
            return;
        }
        //计算分数
        //是否精华
        boolean wonderful = post.getStatus() == 1;
        //评论数量
        int commentCount = post.getCommentCount();
        //点赞数量
        long likeCount = likesService.getLikeCount(ConstantVO.ENTITY_TYPE_POST, postId);
        //计算权重
        double weight=(wonderful?75:0)+commentCount*10+likeCount*2;
        //分数
        double score=Math.log10(Math.max(weight,1))+(post.getCreateTime().getTime()-
                epoch.toInstant(ZoneOffset.of("+8")).toEpochMilli())/(1000*3600*24);
        //更新帖子分数
        discussPostService.updateScore(postId,score);
        //更新es中的数据
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);
    }
}