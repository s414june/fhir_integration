package com.example.fhir_etl_test;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DownloadJsonController extends HttpServlet {
    // @PostMapping("/api/downloadjson")
    @GetMapping("/api/downloadjson")
    public ResponseEntity<?> getSearchResultViaAjax(HttpServletRequest request, HttpServletResponse response,
            @Validated @RequestBody String datas, Errors errors)
            throws Exception {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss");
        String time = LocalDateTime.now().format(dateTimeFormatter);
        
        File f = new File("config" + time + ".json");
        FileWriter fw = new FileWriter(f);
        fw.write(String.valueOf(datas));
        fw.close();

        return ResponseEntity.ok("");
    }

}
