package xyz.rexlin600.java8.stream;

import org.junit.Before;
import org.junit.Test;
import xyz.rexlin600.model.Goods;

import static org.junit.Assert.*;

/**
 * @description
 * @auther hekunlin
 * @create 2020-01-09 14:15
 */
public class ReduceTest {

    private static final String PATTERN = "yyyy-MM-dd HH:mm:ss";

    Reduce reduce;

    @Before
    public void setUp() throws Exception {
        reduce = new Reduce();
    }

    @Test
    public void reduce() {
        Goods goods = reduce.reduce();

        assertEquals("Annnora", goods.getName());
    }

    @Test
    public void reduce1() {
        Goods reduce = this.reduce.reduceByIdentity();
        System.out.println(reduce);

    }
}