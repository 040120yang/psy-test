package com.psy.business.mapper;

import com.psy.business.domain.Patient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

/**
 * 学生信息数据层
 */
@Mapper
public interface PatientMapper {

    /** 分页/条件查询学生列表 */
    List<Patient> selectPatientList(@Param("patient") Patient patient);

    /** 根据ID查询学生 */
    @Select("select * from psy_patient where patient_id = #{patientId}")
    Patient selectPatientById(@Param("patientId") Long patientId);

    /** 新增学生 */
    int insertPatient(Patient patient);

    /** 修改学生 */
    int updatePatient(Patient patient);

    /** 根据用户ID更新学生档案 */
    int updatePatientByUserId(Patient patient);

    /** 根据用户ID查询学生 */
    Patient selectPatientByUserId(@Param("userId") Long userId);

    /** 删除学生 */
    @Delete("delete from psy_patient where patient_id = #{patientId}")
    int deletePatientById(@Param("patientId") Long patientId);
}
