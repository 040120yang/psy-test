package com.psy.business.service.impl;

import com.psy.business.domain.Patient;
import com.psy.business.mapper.PatientMapper;
import com.psy.business.service.PatientService;
import com.psy.common.core.PageUtils;
import com.psy.common.core.TableDataInfo;
import com.psy.common.exception.ServiceException;
import com.psy.common.utils.StringUtils;
import com.psy.framework.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 患者信息业务实现
 */
@Service
public class PatientServiceImpl implements PatientService {

    @Autowired
    private PatientMapper patientMapper;

    @Override
    public TableDataInfo list(Patient patient) {
        // 公众用户只能查看/管理关联到自己的患者
        if (SecurityUtils.isUser()) {
            patient.setUserId(SecurityUtils.getUserId());
        }
        PageUtils.startPage();
        List<Patient> list = patientMapper.selectPatientList(patient);
        return PageUtils.getDataTable(list);
    }

    @Override
    public int add(Patient patient) {
        if (StringUtils.isEmpty(patient.getPatientName())) {
            throw new ServiceException("患者姓名不能为空");
        }
        if (SecurityUtils.isUser()) {
            patient.setUserId(SecurityUtils.getUserId());
        }
        return patientMapper.insertPatient(patient);
    }

    @Override
    public int edit(Patient patient) {
        if (patient.getPatientId() == null) {
            throw new ServiceException("患者ID不能为空");
        }
        Patient exist = patientMapper.selectPatientById(patient.getPatientId());
        if (exist == null) {
            throw new ServiceException("患者不存在");
        }
        // 公众用户只能修改自己的患者
        if (SecurityUtils.isUser() && exist.getUserId() != null
                && !SecurityUtils.getUserId().equals(exist.getUserId())) {
            throw new ServiceException("无权限修改该患者信息");
        }
        return patientMapper.updatePatient(patient);
    }

    @Override
    public int remove(Long patientId) {
        Patient exist = patientMapper.selectPatientById(patientId);
        if (exist == null) {
            throw new ServiceException("患者不存在");
        }
        if (SecurityUtils.isUser() && exist.getUserId() != null
                && !SecurityUtils.getUserId().equals(exist.getUserId())) {
            throw new ServiceException("无权限删除该患者信息");
        }
        return patientMapper.deletePatientById(patientId);
    }
}
