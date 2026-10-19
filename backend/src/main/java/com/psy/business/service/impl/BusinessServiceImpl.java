package com.psy.business.service.impl;

import com.psy.business.domain.Knowledge;
import com.psy.business.domain.Message;
import com.psy.business.mapper.BusinessMapper;
import com.psy.business.service.BusinessService;
import com.psy.common.exception.ServiceException;
import com.psy.common.utils.StringUtils;
import com.psy.framework.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 消息中心、心理知识库、心理评估与系统日志业务实现
 *

 */
@Service
public class BusinessServiceImpl implements BusinessService {

    private static final Logger log = LoggerFactory.getLogger(BusinessServiceImpl.class);

    /** 日志类接口单次最多返回条数 */
    private static final int LOG_LIMIT = 200;

    /** 历史对比最多返回的测评记录数 */
    private static final int COMPARE_LIMIT = 20;

    /** 文章已发布状态值 */
    private static final Integer STATUS_PUBLISHED = 1;

    /** 评估结果通知的标题与类型 */
    private static final String DIAGNOSIS_TITLE = "测评评估结果通知";
    private static final String MESSAGE_TYPE_RESULT = "result";

    @Autowired
    private BusinessMapper businessMapper;

    //  消息中心

    @Override
    public List<Message> myMessages() {
        return businessMapper.selectMyMessages(SecurityUtils.getUserId());
    }

    @Override
    public void readMessage(Long id) {
        if (id == null) {
            throw new ServiceException("消息ID不能为空");
        }
        businessMapper.markMessageRead(id);
    }

    @Override
    public int unreadCount() {
        return businessMapper.countUnreadMessages(SecurityUtils.getUserId());
    }

    @Override
    public void sendMessage(Message message) {
        requireAdminOrDoctor("无权限操作，仅心理教师或系统管理员可下发通知");
        if (message == null || StringUtils.isEmpty(message.getTitle())) {
            throw new ServiceException("消息标题不能为空");
        }
        if (StringUtils.isEmpty(message.getContent())) {
            throw new ServiceException("消息内容不能为空");
        }
        businessMapper.insertMessage(message);
    }

    // 心理知识库

    @Override
    public List<Knowledge> knowledgeList(String category) {
        return businessMapper.selectKnowledgeList(category);
    }

    @Override
    public Knowledge knowledgeDetail(Long id) {
        Knowledge knowledge = businessMapper.selectKnowledgeById(id);
        if (knowledge == null) {
            throw new ServiceException("文章不存在");
        }
        boolean staff = SecurityUtils.isAdmin() || SecurityUtils.isDoctor();
        if (!staff && !STATUS_PUBLISHED.equals(knowledge.getStatus())) {
            throw new ServiceException("文章不存在或已下架");
        }
        businessMapper.increaseKnowledgeView(id);
        return knowledge;
    }

    @Override
    public void addKnowledge(Knowledge knowledge) {
        requireAdminOrDoctor("无权限操作，仅系统管理员或医护人员可管理知识库");
        if (knowledge == null || StringUtils.isEmpty(knowledge.getTitle())) {
            throw new ServiceException("文章标题不能为空");
        }
        businessMapper.insertKnowledge(knowledge);
    }

    @Override
    public void updateKnowledge(Knowledge knowledge) {
        requireAdminOrDoctor("无权限操作，仅系统管理员或医护人员可管理知识库");
        if (knowledge == null || knowledge.getId() == null) {
            throw new ServiceException("文章ID不能为空");
        }
        businessMapper.updateKnowledge(knowledge);
    }

    @Override
    public void deleteKnowledge(Long id) {
        requireAdminOrDoctor("无权限操作，仅系统管理员或医护人员可管理知识库");
        if (id == null) {
            throw new ServiceException("文章ID不能为空");
        }
        businessMapper.deleteKnowledge(id);
    }

    //  心理评估

    @Override
    public void saveDiagnosis(Map<String, Object> body) {
        requireAdminOrDoctor("无权限操作，仅系统管理员或心理教师可填写心理评估");

        Long recordId = parseRecordId(body == null ? null : body.get("recordId"));
        String conclusion = toText(body == null ? null : body.get("conclusion"));
        String advice = toText(body == null ? null : body.get("advice"));

        Map<String, Object> owner = businessMapper.selectRecordOwner(recordId);
        if (owner == null) {
            throw new ServiceException("测评记录不存在");
        }
        Long userId = toLong(owner.get("userId"));

        businessMapper.updateDiagnosis(recordId, conclusion, advice);

        if (userId != null) {
            notifyDiagnosisResult(userId, conclusion, advice);
        }
    }

    //  历史对比

    @Override
    public List<Map<String, Object>> compareHistory() {
        return businessMapper.selectCompareHistory(SecurityUtils.getUserId(), COMPARE_LIMIT);
    }

    //  系统日志

    @Override
    public List<Map<String, Object>> operLog() {
        requireAdmin();
        return businessMapper.selectOperLog(LOG_LIMIT);
    }

    @Override
    public List<Map<String, Object>> loginLog() {
        requireAdmin();
        return businessMapper.selectLoginLog(LOG_LIMIT);
    }

    //  内部方法

    /**
     * 向学生推送评估结果通知
     *
     * <p>通知属于附加动作，写入失败不影响评估结论入库，仅记录日志。</p>
     *
     * @param userId     接收人用户ID
     * @param conclusion 评估结论
     * @param advice     关怀建议
     */
    private void notifyDiagnosisResult(Long userId, String conclusion, String advice) {
        Message notice = new Message();
        notice.setUserId(userId);
        notice.setTitle(DIAGNOSIS_TITLE);
        notice.setContent(buildDiagnosisContent(conclusion, advice));
        notice.setType(MESSAGE_TYPE_RESULT);
        try {
            businessMapper.insertMessage(notice);
        } catch (Exception e) {
            log.error("测评评估通知推送失败，userId={}", userId, e);
        }
    }

    /**
     * 拼接评估结果通知正文
     *
     * @param conclusion 评估结论
     * @param advice     关怀建议
     * @return 通知正文
     */
    private String buildDiagnosisContent(String conclusion, String advice) {
        StringBuilder content = new StringBuilder("您的测评记录已完成心理教师评估。");
        if (StringUtils.isNotEmpty(conclusion)) {
            content.append("评估结论：").append(conclusion).append("；");
        }
        if (StringUtils.isNotEmpty(advice)) {
            content.append("关怀建议：").append(advice).append("；");
        }
        content.append("详情可在“我的记录”中查看。");
        return content.toString();
    }

    /**
     * 解析并校验测评记录ID
     *
     * @param value 原始值
     * @return 测评记录ID
     */
    private Long parseRecordId(Object value) {
        if (value == null || StringUtils.isEmpty(value.toString())) {
            throw new ServiceException("测评记录ID不能为空");
        }
        try {
            return Long.valueOf(value.toString().trim());
        } catch (NumberFormatException e) {
            throw new ServiceException("测评记录ID格式不正确");
        }
    }

    /**
     * 将请求值转为字符串，null 原样返回
     *
     * @param value 原始值
     * @return 字符串
     */
    private String toText(Object value) {
        return value == null ? null : value.toString();
    }

    /**
     * 将数据库返回值转为 Long，null 原样返回
     *
     * @param value 原始值
     * @return 数值
     */
    private Long toLong(Object value) {
        if (value == null || StringUtils.isEmpty(value.toString())) {
            return null;
        }
        return Long.valueOf(value.toString());
    }

    /** 校验当前用户是否为系统管理员 */
    private void requireAdmin() {
        if (!SecurityUtils.isAdmin()) {
            throw new ServiceException("无权限查看系统日志");
        }
    }

    /**
     * 校验当前用户是否为系统管理员或心理教师
     *
     * @param message 不满足条件时的提示语
     */
    private void requireAdminOrDoctor(String message) {
        if (!SecurityUtils.isAdmin() && !SecurityUtils.isDoctor()) {
            throw new ServiceException(message);
        }
    }
}
