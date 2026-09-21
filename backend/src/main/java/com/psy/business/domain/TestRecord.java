package com.psy.business.domain;

import com.psy.common.core.BaseEntity;

import java.math.BigDecimal;

/**
 * 心理测评记录对象 psy_test_record
 */
public class TestRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long recordId;

    /** 测评用户ID */
    private Long userId;

    /** 量表ID */
    private Long scaleId;

    /** 原始粗分 */
    private Integer rawScore;

    /** 标准分 */
    private BigDecimal stdScore;

    /** 结果等级（正常/轻度/中度/重度） */
    private String level;

    /** 结果分析与建议 */
    private String suggestion;

    /** 状态（1已完成） */
    private String status;

    /** 量表名称（联查） */
    private String scaleName;

    /** 量表编码（联查） */
    private String scaleCode;

    /** 用户昵称（联查） */
    private String nickname;

    /** 用户账号（联查） */
    private String username;

    /** 医生诊断结论 */
    private String doctorConclusion;

    /** 医生处方建议 */
    private String doctorAdvice;

    public String getDoctorConclusion() { return doctorConclusion; }
    public void setDoctorConclusion(String doctorConclusion) { this.doctorConclusion = doctorConclusion; }
    public String getDoctorAdvice() { return doctorAdvice; }
    public void setDoctorAdvice(String doctorAdvice) { this.doctorAdvice = doctorAdvice; }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getScaleId() {
        return scaleId;
    }

    public void setScaleId(Long scaleId) {
        this.scaleId = scaleId;
    }

    public Integer getRawScore() {
        return rawScore;
    }

    public void setRawScore(Integer rawScore) {
        this.rawScore = rawScore;
    }

    public BigDecimal getStdScore() {
        return stdScore;
    }

    public void setStdScore(BigDecimal stdScore) {
        this.stdScore = stdScore;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
