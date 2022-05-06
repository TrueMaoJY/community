package com.maomao.community.config;

import com.maomao.community.quartz.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

/**
 * @author MaoJY
 * @create 2022-05-01 19:11
 * @Description:
 */
@Configuration
public class QuartzConfig {
    /**
    * Description:刷新帖子分数的任务
    * date: 2022/5/1 19:13
    * @author: MaoJY
    * @since JDK 1.8
    */
    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail(){
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        jobDetailFactoryBean.setJobClass(PostScoreRefreshJob.class);
        jobDetailFactoryBean.setDurability(true);
        jobDetailFactoryBean.setName("postScoreRefreshJob");
        jobDetailFactoryBean.setGroup("communityJobGroup");
        jobDetailFactoryBean.setRequestsRecovery(true);
        return  jobDetailFactoryBean;
    }
    /**
    * Description:任务激发器
    * date: 2022/5/1 19:16
    * @author: MaoJY
    * @since JDK 1.8
    */
    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setGroup("communityTriggerGroup");
        factoryBean.setName("postScoreRefreshTrigger");
        factoryBean.setJobDetail(postScoreRefreshJobDetail);
        factoryBean.setRepeatInterval(1000*60*60*24);
        factoryBean.setJobDataMap(new JobDataMap());
        return  factoryBean;
    }
}