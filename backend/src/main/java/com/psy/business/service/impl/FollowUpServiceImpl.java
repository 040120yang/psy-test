package com.psy.business.service.impl;

import com.psy.business.domain.FollowUp;
import com.psy.business.mapper.FollowUpMapper;
import com.psy.business.service.FollowUpService;
import com.psy.common.core.PageUtils;
import com.psy.common.core.TableDataInfo;
import com.psy.framework.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 随访业务实现
 *
 */
@Service
public class FollowUpServiceImpl implements FollowUpService {

    private static final Logger log = LoggerFactory.getLogger(FollowUpServiceImpl.class);

    /** 待随访 */
    private static final String STATUS_PENDING = "待随访";

    /** 已完成 */
    private static final String STATUS_FINISHED = "已完成";

    /** 已逾期（查询时计算出的展示状态） */
    private static final String STATUS_OVERDUE = "已逾期";

    /** 随访提醒消息标题 */
    private static final String NOTICE_TITLE = "随访提醒";

    /** 随访提醒消息类型 */
    private static final String NOTICE_TYPE = "follow";

    /** 日期格式 */
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    @Autowired
    private FollowUpMapper followUpMapper;

    @Override
    public TableDataInfo adminList(String status) {
        PageUtils.startPage();
        List<FollowUp> list = followUpMapper.selectList(null, status);
        return PageUtils.getDataTable(list);
    }

    @Override
    public TableDataInfo myList() {
        Long userId = SecurityUtils.getUserId();
        List<FollowUp> list = followUpMapper.selectList(userId, null);
        return TableDataInfo.success(list, list.size());
    }

    @Override
    public FollowUp getById(Long id) {
        return followUpMapper.selectById(id);
    }

    @Override
    public int add(FollowUp followUp) {
        followUp.setStatus(STATUS_PENDING);
        int rows = followUpMapper.insert(followUp);
        sendFollowNotice(followUp);
        return rows;
    }

    @Override
    public int update(FollowUp followUp) {
        // 「已逾期」是派生状态，编辑表单回填后可能原样提交，此处统一归一，避免污染状态字段
        if (STATUS_OVERDUE.equals(followUp.getStatus())) {
            followUp.setStatus(STATUS_PENDING);
        }
        return followUpMapper.update(followUp);
    }

    @Override
    public int finish(Long id, String doctorNote, Integer symptomScore, String nextFollowDate) {
        FollowUp followUp = new FollowUp();
        followUp.setId(id);
        followUp.setStatus(STATUS_FINISHED);
        followUp.setDoctorNote(doctorNote);
        followUp.setSymptomScore(symptomScore);
        followUp.setNextFollowDate(parseDate(nextFollowDate));
        return followUpMapper.update(followUp);
    }

    @Override
    public int delete(Long id) {
        return followUpMapper.deleteById(id);
    }

    @Override
    public TableDataInfo overdue() {
        List<FollowUp> list = followUpMapper.selectOverdue();
        return TableDataInfo.success(list, list.size());
    }

    /**
     * 发送随访提醒消息。消息写入失败不影响随访本身的创建。
     *
     * @param followUp 已入库的随访记录（需包含主键、学生ID与随访日期）
     */
    private void sendFollowNotice(FollowUp followUp) {
        if (followUp.getUserId() == null) {
            return;
        }
        try {
            String dateText = followUp.getFollowDate() == null
                    ? ""
                    : new SimpleDateFormat(DATE_PATTERN).format(followUp.getFollowDate());
            String content = "您有一条新的随访任务，随访日期：" + dateText + "，请及时处理。";
            followUpMapper.insertNotice(followUp.getUserId(), NOTICE_TITLE, content, NOTICE_TYPE);
        } catch (Exception e) {
            log.error("发送随访提醒失败，随访ID={}，学生ID={}", followUp.getId(), followUp.getUserId(), e);
        }
    }

    /**
     * 解析 yyyy-MM-dd 格式的日期，空值或格式非法时返回 null。
     *
     * @param text 日期文本
     * @return 解析结果，无法解析时为 null
     */
    private Date parseDate(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN);
        format.setLenient(false);
        try {
            return format.parse(text.trim());
        } catch (ParseException e) {
            log.warn("下次随访日期格式不正确，已忽略：{}", text);
            return null;
        }
    }
}
