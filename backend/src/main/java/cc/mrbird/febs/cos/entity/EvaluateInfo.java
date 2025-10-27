package cc.mrbird.febs.cos.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 
 *
 * @author FanK
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class EvaluateInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 作业人员ID
     */
    private Integer staffId;

    /**
     * 年份
     */
    private Integer year;

    /**
     * 季度
     */
    private Integer quarterly;

    /**
     * 评价人
     */
    private String evaluateUser;

    /**
     * 质量分数
     */
    private BigDecimal qualityScore;

    /**
     * 效率分数
     */
    private BigDecimal efficiencyScore;

    /**
     * 满意分数
     */
    private BigDecimal satisfactionScore;

    /**
     * 评价总分
     */
    private BigDecimal totalScore;

    /**
     * 团队分数
     */
    private BigDecimal teamScore;

    /**
     * 评价时间
     */
    private String createDate;

    /**
     * 员工姓名
     */
    @TableField(exist = false)
    private String staffName;

    /**
     * 员工类型
     */
    @TableField(exist = false)
    private String type;

    /**
     * 评价内容
     */
    private String evaluateInfoList;


}
