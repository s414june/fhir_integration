package com.example.fhir_etl_test;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;

// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;
/**
 * Created by javadeveloperzone on 15-12-2017.
 */
// @RestController
// // @RequestMapping("/api-2.0")
// public class WebController {
//     @RequestMapping("/demo")
//     public String getData(HttpServletRequest request, HttpServletResponse response){
//         return "a";
//     }
// }
@Controller
public class WebController {
    @GetMapping("/a")
    public String a() {
        return "a"; // 要導入的html
    }
    @GetMapping("/download")
    public void download(HttpServletRequest request, HttpServletResponse response)
    throws Exception {
        String datas = "";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss");
        String time = LocalDateTime.now().format(dateTimeFormatter);
        javax.servlet.http.Cookie[] cookies = request.getCookies();
        for (javax.servlet.http.Cookie cookie : cookies) {
            if (cookie.getName().equals("fhirconfigdata")) {
                datas = cookie.getValue();
            }
          }
        PrintWriter out = response.getWriter();
        out.flush();
        out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode("config" + time, "utf-8"));
        out.print(datas);
        out.flush();
        // return "download";
    }
}