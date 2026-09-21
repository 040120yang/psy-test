package com.psy.business.domain;

import java.util.Date;

/**
 * 随访管理
 */
public class FollowUp {
    private Long id;
    private Long userId;
    private Long recordId;
    private Long doctorId;
    private Date followDate;
    private String followType;
    private String status;
    private Integer symptomScore;
    private String doctorNote;
    private Date nextFollowDate;
    private Date createTime;

    // 联表字段
    private String username;
    private String nickname;
    private String scaleName;
    private String level;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public Date getFollowDate() { return followDate; }
    public void setFollowDate(Date followDate) { this.followDate = followDate; }
    public String getFollowType() { return followType; }
    public void setFollowType(String followType) { this.followType = followType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getSymptomScore() { return symptomScore; }
    public void setSymptomScore(Integer symptomScore) { this.symptomScore = symptomScore; }
    public String getDoctorNote() { return doctorNote; }
    public void setDoctorNote(String doctorNote) { this.doctorNote = doctorNote; }
    public Date getNextFollowDate() { return nextFollowDate; }
    public void setNextFollowDate(Date nextFollowDate) { this.nextFollowDate = nextFollowDate; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getScaleName() { return scaleName; }
    public void setScaleName(String scaleName) { this.scaleName = scaleName; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
}
