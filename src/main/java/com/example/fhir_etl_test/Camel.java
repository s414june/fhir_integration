package com.example.fhir_etl_test;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultRegistry;
import org.springframework.stereotype.Component;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

@Component
public class Camel {
    CamelContext context = new DefaultCamelContext();
    // private static final Logger log = LoggerFactory.getLogger(Camel.class);

    public void run1() {
        System.out.println("哈囉");
        // log.info("哈囉");
        try {
            context.addRoutes(new RouteBuilder() {
                public void configure() throws Exception {
                    try {
                        // noop=true表示將不刪掉此檔案
                        from("file:///Users/june.wu/Desktop/cameltest/fileMove?noop=true")
                                .log("${headers}")
                                .log("${body}")
                                // 若無fileMovePlace資料夾會自動新增
                                .to("file:///Users/june.wu/Desktop/cameltest/fileMove/fileMovePlace")
                                .end();
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            });
            context.start();
            Thread.sleep(10000);
            context.close();
        } catch (Exception e) {
            // log.error(e.getStackTrace().toString());
        } finally {
            context.stop();
            // log.info("finally!");
        }
    }

    public void run2() throws Exception {
        SQLServerDataSource SampleDBSource = setupDataSource("SampleDB");
        SQLServerDataSource testdbSource = setupDataSource("testdb");
        DefaultRegistry reg = new DefaultRegistry();
        reg.bind("SampleDBSource", SampleDBSource);
        reg.bind("testdbSource", testdbSource);
        CamelContext context = new DefaultCamelContext(reg);
        context.addRoutes(new MyRouteBuilder());
        context.start();
        Thread.sleep(5000);
        context.stop();
        context.close();
    }

    private static SQLServerDataSource setupDataSource(String databaseName) {
        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setUser("june");
        ds.setPassword("0000");
        ds.setServerName("localhost\\\\SQLEXPRESS;");
        ds.setPortNumber(1433);
        ds.setDatabaseName(databaseName);
        ds.setTrustServerCertificate(true);

        return ds;
    }
    
    class MyRouteBuilder extends RouteBuilder {
        public void configure() throws Exception {
            String sql = "SELECT [Name],[Location] FROM [Employees]";
            String insertSqlStr = "insert into [Table_Json] (jsondata) values('${body}'); ";
            from("timer://foo?repeatCount=1")
                    .setBody(constant(sql))
                    .to("jdbc:SampleDBSource")
                    // 用split切開，將一次只存一筆(員工+國家)的資料；沒切將會把所有資料包成一筆
                    // .split(body())
                    .process(
                            new Processor() {
                                public void process(Exchange exchange) throws Exception {
                                    String body = exchange.getIn().getBody(String.class);// 官方固定用法
                                    System.out.println(body);
                                    log.debug(body);
                                }
    
                            })
                    .setBody(simple(insertSqlStr))
                    .to("jdbc:testdbSource");
        }
    }
}