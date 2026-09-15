package com.psy.business.domain;

import com.psy.common.core.BaseEntity;

/**
 * 量表题目对象 psy_question
 */
public class Question extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 题目ID */
    private Long questionId;

    /** 所属量表ID */
    private Long scaleId;

    /** 题号 */
    private Integer sortNo;

    /** 题目内容 */
    private String content;

    /** 是否反向计分（0否 1是） */
    private Integer reverseFlag;

    /** 量表名称（联查） */
    private String scaleName;

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getScaleId() {
        return scaleId;
    }

    public void setScaleId(Long scaleId) {
        this.scaleId = scaleId;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getReverseFlag() {
        return reverseFlag;
    }

    public void setReverseFlag(Integer reverseFlag) {
        this.reverseFlag = reverseFlag;
    }

    public String getScaleName() {
        return scaleName;
    }

    public void setScaleName(String scaleName) {
        this.scaleName = scaleName;
    }
}
