package xyz.rexlin600.swagger2.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author rexlin600
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message implements Serializable {

    private Long id;

    @ApiModelProperty(value = "消息体")
    private String text;

    @ApiModelProperty(value = "消息总结")
    private String summary;

    private Date createDate;

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", summary='" + summary + '\'' +
                ", createDate=" + createDate +
                '}';
    }
}