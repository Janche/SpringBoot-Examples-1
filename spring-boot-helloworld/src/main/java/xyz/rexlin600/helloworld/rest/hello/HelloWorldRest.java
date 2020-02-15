package xyz.rexlin600.helloworld.rest.hello;

import org.springframework.web.bind.annotation.*;
import xyz.rexlin600.helloworld.entity.req.PostReq;

/**
 * @menu HelloWorld
 *
 * @author: hekunlin
 */
@RestController
@RequestMapping(value = "/hello")
public class HelloWorldRest {

    private static String HELLO_WORLD = "hello world";

    /**
     * 1. 【GET请求】
     *
     * @return
     */
    @GetMapping("/get")
    public String get() {
        return HELLO_WORLD.toUpperCase();
    }

    /**
     * 2. 【POST请求】
     *
     * @return
     */
    @PostMapping("/post")
    public String post(@RequestBody PostReq postReq) {
        return HELLO_WORLD.toUpperCase() + " : name=" + postReq.getName().toUpperCase() + " ,age=" + postReq.getAge();
    }

    /**
     * 3. 【PUT请求】
     *
     * @return
     */
    @PutMapping("put/{id}")
    public String put(@PathVariable(value = "id") Long id) {
        return HELLO_WORLD.toUpperCase() + " : " + id;
    }

    /**
     * 4. 【DELETE请求】
     *
     * @return
     */
    @DeleteMapping("delete/{id}")
    public String delete(@PathVariable(value = "id") Long id) {
        return HELLO_WORLD.toUpperCase() + " : " + id;
    }


}