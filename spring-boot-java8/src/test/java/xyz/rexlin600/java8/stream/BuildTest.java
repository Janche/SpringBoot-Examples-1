package xyz.rexlin600.java8.stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @description
 * @auther hekunlin
 * @create 2020-01-09 10:47
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class BuildTest {

    private Build build;

    @Before
    public void setUp() throws Exception {
        build = new Build();
    }

    @Test
    public void buildStream() {
        build.buildStream();
    }
}