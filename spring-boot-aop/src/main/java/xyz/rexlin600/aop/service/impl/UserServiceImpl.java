package xyz.rexlin600.aop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import xyz.rexlin600.aop.entity.User;
import xyz.rexlin600.aop.mapper.UserMapper;
import xyz.rexlin600.aop.service.UserService;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author rexlin600
 * @since 2020-02-16
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}