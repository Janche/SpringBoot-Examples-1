package xyz.rexlin600.runner.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author: hekunlin
 * @since: 2020/1/2
 */
@Component
@Slf4j
@Order(1)
public class FirstApplicationRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("==>  ApplicationRunner start " + this.getClass().getName() + " , args=[" + args + "] ...");
    }

}