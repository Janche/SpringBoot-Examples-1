package xyz.rexlin600.java8.date.api;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @description
 * @auther hekunlin
 * @create 2020-01-10 11:21
 */
public class DateTimeFormatterApiTest {

    DateTimeFormatterApi dateTimeFormatterApi;

    @Before
    public void setUp() throws Exception {
        dateTimeFormatterApi = new DateTimeFormatterApi();
    }

    @Test
    public void format() {
        dateTimeFormatterApi.format();
    }
}