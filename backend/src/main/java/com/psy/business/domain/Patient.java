package com.psy.business.domain;

import com.psy.common.core.BaseEntity;

/**
 * 患者信息对象 psy_patient
 */
public class Patient extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 患者ID */
    private Long patientId;

    /** 关联注册用户ID */
    private Long userId;

    /** 患者姓名 */
    private String patientName;

    /** 性别（0男 1女 2未知） */
    private String sex;

    /** 年龄 */
    private Integer age;

    /** 联系电话 */
    private String phone;

    /** 身份证号 */
    private String idCard;

    /** 联系地址 */
    private String address;

    /** 病史/主诉 */
    private String medicalHistory;

    /** 患者标签 */
    private String tag;

    /** 关联用户账号（联查） */
    private String username;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
