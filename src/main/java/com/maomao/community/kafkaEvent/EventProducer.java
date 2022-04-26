package com.maomao.community.kafkaEvent;

import com.maomao.community.entity.Event;
import com.maomao.community.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author MaoJY
 * @create 2022-04-19 16:59
 * @Description:
 */
@Component
public class EventProducer {
    @Autowired
    private KafkaTemplate kafkaTemplate;
    public void fireEvent(Event event){
        kafkaTemplate.send(event.getTopic(), JsonUtil.object2JsonStr(event));
    }
}