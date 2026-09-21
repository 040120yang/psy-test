package com.psy.business.service;

import com.psy.business.domain.FollowUp;
import com.psy.business.mapper.FollowUpMapper;
import com.psy.common.core.TableDataInfo;
import com.psy.framework.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FollowUpService {

    @Autowired
    private FollowUpMapper followUpMapper;

    public TableDataInfo adminList(String status) {
        List<FollowUp> list = followUpMapper.selectList(null, status);
        return TableDataInfo.success(list, list.size());
    }

    public TableDataInfo myList() {
        Long userId = SecurityUtils.getUserId();
        List<FollowUp> list = followUpMapper.selectList(userId, null);
        return TableDataInfo.success(list, list.size());
    }

    public FollowUp getById(Long id) {
        return followUpMapper.selectById(id);
    }

    public int add(FollowUp followUp) {
        followUp.setStatus("待随访");
        return followUpMapper.insert(followUp);
    }

    public int update(FollowUp followUp) {
        return followUpMapper.update(followUp);
    }

    public int finish(Long id, String doctorNote, Integer symptomScore, String nextFollowDate) {
        FollowUp f = new FollowUp();
        f.setId(id);
        f.setStatus("已完成");
        f.setDoctorNote(doctorNote);
        f.setSymptomScore(symptomScore);
        if (nextFollowDate != null && !nextFollowDate.isEmpty()) {
            try {
                f.setNextFollowDate(new java.text.SimpleDateFormat("yyyy-MM-dd").parse(nextFollowDate));
                // 自动创建下次随访任务
                FollowUp next = new FollowUp();
                next.setUserId(followUpMapper.selectById(id).getUserId());
                next.setRecordId(followUpMapper.selectById(id).getRecordId());
                next.setFollowDate(new java.text.SimpleDateFormat("yyyy-MM-dd").parse(nextFollowDate));
                next.setFollowType("线上");
                next.setStatus("待随访");
                followUpMapper.insert(next);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return followUpMapper.update(f);
    }

    public int delete(Long id) {
        return followUpMapper.deleteById(id);
    }

    public TableDataInfo overdue() {
        List<FollowUp> list = followUpMapper.selectOverdue();
        return TableDataInfo.success(list, list.size());
    }
}
