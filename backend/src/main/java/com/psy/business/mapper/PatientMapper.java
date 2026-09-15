package com.psy.business.mapper;

import com.psy.business.domain.Patient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

/**
 * 患者信息数据层
 */
@Mapper
public interface PatientMapper {

    /** 分页/条件查询患者列表 */
    List<Patient> selectPatientList(@Param("patient") Patient patient);

    /** 根据ID查询患者 */
    @Select("select * from psy_patient where patient_id = #{patientId}")
    Patient selectPatientById(@Param("patientId") Long patientId);

    /** 新增患者 */
    int insertPatient(Patient patient);

    /** 修改患者 */
    int updatePatient(Patient patient);

    /** 删除患者 */
    @Delete("delete from psy_patient where patient_id = #{patientId}")
    int deletePatientById(@Param("patientId") Long patientId);
}
