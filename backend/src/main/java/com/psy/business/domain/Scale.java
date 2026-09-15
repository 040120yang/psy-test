package com.psy.business.domain;

import com.psy.common.core.BaseEntity;

/**
 * 心理量表对象 psy_scale
 */
public class Scale extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 量表ID */
    private Long scaleId;

    /** 量表名称 */
    private String scaleName;

    /** 量表编码（SAS/SDS/SCL90/SRSS） */
    private String scaleCode;

    /** 量表说明 */
    private String description;

    /** 选项评分制（4=四级评分 5=五级评分） */
    private Integer optionType;

    /** 状态（0启用 1停用） */
    private String status;

    /** 显示顺序 */
    private Integer sortNo;

    /** 题目数量（统计字段） */
    private Integer questionCount;

    public Long getScaleId() {
        return scaleId;
    }

    public void setScaleId(Long scaleId) {
        this.scaleId = scaleId;
    }

    public String getScaleName() {
        return scaleName;
    }

    public void setScaleName(String scaleName) {
        this.scaleName = scaleName;
    }

    public String getScaleCode() {
        return scaleCode;
    }

    public void setScaleCode(String scaleCode) {
        this.scaleCode = scaleCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getOptionType() {
        return optionType;
    }

    public void setOptionType(Integer optionType) {
        this.optionType = optionType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }
}
