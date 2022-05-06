package com.maomao.community.controller;

import com.maomao.community.entity.Event;
import com.maomao.community.kafkaEvent.EventProducer;
import com.maomao.community.util.CommunityUtil;
import com.maomao.community.vo.ConstantVO;
import com.maomao.community.vo.RespBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author MaoJY
 * @create 2022-05-02 18:50
 * @Description:分享长图
 */
@Controller
@Slf4j
public class ShareController {
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Value("${wk.image.storage}")
    private String wkImageStorage;
    @Autowired
    private EventProducer eventProducer;
    @RequestMapping("/share")
    @ResponseBody
    public RespBean share(String htmlUrl){
        //文件名
        String fileName = CommunityUtil.generateUUID();
        String suffix=".png";
        //异步生成长图
        Event event = new Event()
                .setTopic(ConstantVO.TOPIC_SHARE)
                .setData("htmlUrl",htmlUrl)
                .setData("fileName",fileName)
                .setData("suffix",suffix);
        eventProducer.fireEvent(event);
        //返回访问路径
        String shareUrl=domain+contextPath+"/share/image/"+fileName;
        return RespBean.success(shareUrl);
    }
    @RequestMapping("/share/image/{fileName}")
    public void getSharedImage(@PathVariable("fileName")String fileName, HttpServletResponse response){
        if (fileName == null) {
            throw  new IllegalArgumentException("文件名不能为空");
        }
        response.setContentType("image/png");
        File file=new File(wkImageStorage+"/"+fileName+".png");
        try {
            FileInputStream fis = new FileInputStream(file);
            ServletOutputStream os = response.getOutputStream();
            byte[] buffer =new byte[1024];
            int b=0;
            while((b=fis.read(buffer))!=-1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
           log.error("获取长图失败"+e.getMessage());
        }
    }
}