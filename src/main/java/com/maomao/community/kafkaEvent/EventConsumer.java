package com.maomao.community.kafkaEvent;

import com.maomao.community.entity.DiscussPost;
import com.maomao.community.entity.Event;
import com.maomao.community.entity.Message;
import com.maomao.community.service.DiscussPostService;
import com.maomao.community.service.ElasticsearchService;
import com.maomao.community.service.MessageService;
import com.maomao.community.util.JsonUtil;
import com.maomao.community.vo.ConstantVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author MaoJY
 * @create 2022-04-19 16:59
 * @Description:
 */
@Component
@Slf4j
public class EventConsumer {
    @Autowired
    private MessageService messageService;
    @Autowired
    private ElasticsearchService elasticsearchService;
    @Autowired
    private DiscussPostService discussPostService;

    @KafkaListener(topics = {ConstantVO.TOPIC_COMMENT,ConstantVO.TOPIC_FOLLOW,ConstantVO.TOPIC_LIKE})
    public void handlerCommentMessage(ConsumerRecord record){
        if (record == null||record.value()==null) {
            log.error("消息为空");
            return ;
        }
        Event event = JsonUtil.jsonStr2Object(record.value().toString(), Event.class);
        if (event == null) {
            log.error("消息格式错误");
            return;
        }
        //封装message
        Message message = new Message();
        message.setFromId(ConstantVO.SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String,Object> content=new HashMap<>();
        content.put("userId",event.getUserId());
        content.put("entityType",event.getEntityType());
        content.put("entityId",event.getEntityId());
        Map<String, Object> data = event.getData();
        if (data != null&&!data.isEmpty()) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }
        message.setContent(JsonUtil.object2JsonStr(content));
        messageService.insertMessage(message);
    }
    /**
    * Description:消费帖子增加，修改的消息
    * date: 2022/4/26 15:17
    * @author: MaoJY
    * @since JDK 1.8
    */
    @KafkaListener(topics = {ConstantVO.TOPIC_PUBLISH})
    public void handlerPublishMessage(ConsumerRecord record){
        if (record == null||record.value()==null) {
            log.error("消息为空");
            return ;
        }
        Event event = JsonUtil.jsonStr2Object(record.value().toString(), Event.class);
        if (event == null) {
            log.error("消息格式错误");
            return;
        }
        DiscussPost post = discussPostService.findDiscussById(event.getEntityId());
        elasticsearchService.saveDiscussPost(post);
    }
    /**
    * Description:拉黑帖子
    * date: 2022/4/28 20:57
    * @author: MaoJY
    * @since JDK 1.8
    */
    @KafkaListener(topics = {ConstantVO.TOPIC_BLOCK})
    public void handlerBlockMessage(ConsumerRecord record){
        if (record == null||record.value()==null) {
            log.error("消息为空");
            return ;
        }
        Event event = JsonUtil.jsonStr2Object(record.value().toString(), Event.class);
        if (event == null) {
            log.error("消息格式错误");
            return;
        }
        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }

}