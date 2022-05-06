package com.maomao.community.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * @author MaoJY
 * @create 2022-05-02 18:46
 * @Description:启动项目，创建wk图片目录
 */
@Configuration
@Slf4j
public class WkConfig {
    @Value("${wk.image.storage}")
    private String wkImageStorage;
    @PostConstruct
    public void init(){
        File file=new File(wkImageStorage);
        if (!file.exists()) {
            file.mkdir();
            log.info("创建wk图片目录成功："+wkImageStorage);
        }
    }
}