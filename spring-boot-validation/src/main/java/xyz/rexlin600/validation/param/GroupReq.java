package xyz.rexlin600.validation.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.rexlin600.validation.param.group.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 分组校验
 *
 * @author: hekunlin
 * @date: 2020/6/1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupReq implements Serializable {

    /**
     * 姓名
     */
    @NotBlank(message = "参数错误：姓名不可为空", groups = {Name.class})
    private String name;

    /**
     * 班级
     */
    @NotBlank(message = "参数错误：班级不可为空", groups = {Classes.class})
    private String classes;

    /**
     * 年龄
     */
    @NotNull(message = "参数错误：年龄不可为空")
    private Integer age;

}