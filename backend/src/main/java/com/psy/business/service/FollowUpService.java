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

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

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
        int rows = followUpMapper.insert(followUp);
        // 自动发送消息通知用户
        try {
            String dateStr = followUp.getFollowDate() != null
                ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(followUp.getFollowDate())
                : "";
            jdbcTemplate.update(
                "INSERT INTO psy_message (user_id, title, content, type) VALUES (?, '随访提醒', ?, 'follow')",
                followUp.getUserId(),
                "您有一条新的随访任务，随访日期：" + dateStr + "，请及时处理。"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
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
