package xyz.rexlin600.swagger.controller;

import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
import xyz.rexlin600.swagger.config.BaseResult;
import xyz.rexlin600.swagger.model.User;

import java.util.*;


/**
 * @author rexlin600
 * @menu Swagger2-用户
 */
@RestController
@RequestMapping(value = "/user")
public class UserController {

    private static Map<Long, User> users = Collections.synchronizedMap(new HashMap<>());

    /**
     * 1. 【用户-用户列表】
     *
     * @return
     */
    @RequestMapping(value = {""}, method = RequestMethod.GET)
    public List<User> getUserList() {
        return new ArrayList<>(users.values());
    }

    /**
     * 2. 【用户-创建用户】
     *
     * @param user
     * @return
     */
    @ApiOperation(value = "2. 【用户-创建用户】", notes = "根据User对象创建用户")
    @RequestMapping(value = "", method = RequestMethod.POST)
    public BaseResult<User> postUser(@ApiIgnore User user) {
        users.put(user.getId(), user);
        return BaseResult.successWithData(user);
    }

    /**
     * 3. 【用户-获取用户信息】
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public User getUser(@PathVariable Long id) {
        return users.get(id);
    }

    /**
     * 4. 【用户-更新用户信息】
     *
     * @param id
     * @param user
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public BaseResult<User> putUser(@PathVariable Long id, @ApiIgnore User user) {
        User u = users.get(id);
        u.setName(user.getName());
        u.setAge(user.getAge());
        users.put(id, u);
        return BaseResult.successWithData(u);
    }

    /**
     * 5. 【用户-根据ID删除用户】
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String deleteUser(@PathVariable Long id) {
        users.remove(id);
        return "success";
    }

    /**
     * 6. 【用户-根据ID删除用户】
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/ignoreMe/{id}", method = RequestMethod.DELETE)
    public String ignoreMe(@PathVariable Long id) {
        users.remove(id);
        return "success";
    }
}