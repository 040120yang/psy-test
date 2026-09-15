package com.psy.business.service;

import com.psy.business.domain.Patient;
import com.psy.common.core.TableDataInfo;

/**
 * 患者信息业务层
 */
public interface PatientService {

    /** 分页查询患者列表 */
    TableDataInfo list(Patient patient);

    /** 新增患者 */
    int add(Patient patient);

    /** 修改患者 */
    int edit(Patient patient);

    /** 删除患者 */
    int remove(Long patientId);
}
