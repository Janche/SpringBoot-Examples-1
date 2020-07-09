package xyz.rexlin600.skywalking;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Skywalking 启动类
 *
 * @author: rexlin600
 * @since: 2020/5/2
 */
@MapperScan(value = "xyz.rexlin600.skywalking.mapper")
@SpringBootApplication
public class SkywalkingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkywalkingApplication.class, args);
    }

}