package com.maomao.community.controller;

import com.maomao.community.service.DataService;
import com.maomao.community.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * @author MaoJY
 * @create 2022-04-29 22:29
 * @Description:
 */
@RequestMapping("/data")
@Controller
public class DataController {
    @Autowired
    private DataService dataService;

    @RequestMapping("/data")
    public String getData(){
        return  "/site/admin/data";
    }
    @RequestMapping("/uv")
    public String  getUV(@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate uvStartDate,
                         @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate uvEndDate, Model model){
       long uv = dataService.calculateUV(uvStartDate, uvEndDate);

//将localDate转换为date类型
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime startZTD = uvStartDate.atStartOfDay(zoneId);
        Date start = Date.from(startZTD.toInstant());
        ZonedDateTime endZDT = uvEndDate.atStartOfDay(zoneId);
        Date end = Date.from(endZDT.toInstant());

        model.addAttribute("uvResult",uv);
       model.addAttribute("uvStartDate",start);
       model.addAttribute("uvEndDate",end);
        return  "forward:/data/data";
    }
    @RequestMapping("/dau")
    public String  getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate dauStartDate,
                         @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate dauEndDate, Model model){
        long dau = dataService.calculateDAU(dauStartDate, dauEndDate);

//将localDate转换为date类型
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime startZTD = dauStartDate.atStartOfDay(zoneId);
        Date start = Date.from(startZTD.toInstant());
        ZonedDateTime endZDT = dauEndDate.atStartOfDay(zoneId);
        Date end = Date.from(endZDT.toInstant());

        model.addAttribute("dauResult",dau);
        model.addAttribute("dauStartDate",start);
        model.addAttribute("dauEndDate",end);
        return  "forward:/data/data";
    }
}