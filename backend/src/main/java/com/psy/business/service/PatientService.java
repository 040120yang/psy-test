package com.psy.business.service;

import com.psy.business.domain.Patient;
import com.psy.common.core.TableDataInfo;

/**
 * 学生信息业务层
 */
public interface PatientService {

    /** 分页查询学生列表 */
    TableDataInfo list(Patient patient);

    /** 新增学生 */
    int add(Patient patient);

    /** 修改学生 */
    int edit(Patient patient);

    /** 删除学生 */
    int remove(Long patientId);
}
