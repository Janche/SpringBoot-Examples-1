package xyz.rexlin600.mybatisplus.crud;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author rexlin600
 */
@MapperScan(value = {"xyz.rexlin600"})
@SpringBootApplication
public class MyBatisPlusCrudApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyBatisPlusCrudApplication.class, args);
    }
}
