package xyz.rexlin600.swagger.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import xyz.rexlin600.docker.entity.User;

/**
 * UserRepository 类
 *
 * @author: rexlin600
 * @date: 2020-01-11
 */
public interface UserRepository extends JpaRepository<User, Long> {

    User findByName(String name);

    User findByNameAndAge(String name, Integer age);

    @Query("from User u where u.name=:name")
    User findUser(@Param("name") String name);

}