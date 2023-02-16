package com.example.fhir_integration;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

// @Component
public class MssqlConnect extends RouteBuilder {
    // private static final Logger log = LoggerFactory.getLogger(MssqlConnect.class);

    @Override
    public void configure() throws Exception {
        String sql = "SELECT [Name],[Location] FROM [Employees];";
        String insertSqlStr = "insert into [Table_Json] (json_str) values('${body}'); ";
        from("timer://foo?repeatCount=1")
                .setBody(constant(sql))
                .to("jdbc:dataSource")
                // 用split切開，將一次只存一筆(員工+國家)的資料；沒切將會把所有資料包成一筆
                // .split(body())
                .process(
                        new Processor() {
                            public void process(Exchange exchange) throws Exception {
                                String body = exchange.getIn().getBody(String.class);// 官方固定用法
                                System.out.println(body);
                            }

                        })
                .setBody(simple(insertSqlStr))
                .to("jdbc:dataSource");
    }
}