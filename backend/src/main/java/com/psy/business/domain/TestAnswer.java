package com.psy.business.domain;

import com.psy.common.core.BaseEntity;

/**
 * 答题明细对象 psy_test_answer
 */
public class TestAnswer extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 答题ID */
    private Long answerId;

    /** 测评记录ID */
    private Long recordId;

    /** 题目ID */
    private Long questionId;

    /** 选项分值 */
    private Integer optionValue;

    /** 题目内容（联查） */
    private String content;

    /** 题号（联查） */
    private Integer sortNo;

    public Long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Long answerId) {
        this.answerId = answerId;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Integer getOptionValue() {
        return optionValue;
    }

    public void setOptionValue(Integer optionValue) {
        this.optionValue = optionValue;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }
}
