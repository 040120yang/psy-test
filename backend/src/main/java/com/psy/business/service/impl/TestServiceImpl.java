package com.psy.business.service.impl;

import com.psy.business.domain.Question;
import com.psy.business.domain.Scale;
import com.psy.business.domain.TestAnswer;
import com.psy.business.domain.TestRecord;
import com.psy.business.mapper.QuestionMapper;
import com.psy.business.mapper.ScaleMapper;
import com.psy.business.mapper.TestAnswerMapper;
import com.psy.business.mapper.TestRecordMapper;
import com.psy.business.service.TestService;
import com.psy.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 心理测评业务实现（含标准量表计分逻辑）
 */
@Service
public class TestServiceImpl implements TestService {

    @Autowired
    private ScaleMapper scaleMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private TestRecordMapper recordMapper;

    @Autowired
    private TestAnswerMapper answerMapper;

    /**
     * 提交答卷：校验 -> 计分 -> 生成报告 -> 落库
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> submit(Long userId, Long scaleId, List<AnswerItem> answers) {
        if (userId == null) {
            throw new ServiceException("登录状态已失效，请重新登录");
        }
        Scale scale = scaleMapper.selectScaleById(scaleId);
        if (scale == null) {
            throw new ServiceException("量表不存在");
        }
        if ("1".equals(scale.getStatus())) {
            throw new ServiceException("该量表已停用");
        }
        List<Question> questions = questionMapper.selectByScaleId(scaleId);
        if (questions == null || questions.isEmpty()) {
            throw new ServiceException("该量表暂无题目");
        }
        if (answers == null || answers.size() != questions.size()) {
            throw new ServiceException("请完成全部题目后再提交");
        }

        // 1. 计算粗分（含反向计分）
        Map<Long, Integer> answerMap = new HashMap<>();
        for (AnswerItem item : answers) {
            answerMap.put(item.getQuestionId(), item.getOptionValue());
        }
        int rawScore = 0;
        for (Question q : questions) {
            Integer value = answerMap.get(q.getQuestionId());
            if (value == null || value < 1 || value > scale.getOptionType()) {
                throw new ServiceException("存在未作答或选项分值不合法的题目");
            }
            if (q.getReverseFlag() != null && q.getReverseFlag() == 1) {
                // 反向计分：分值 = 满分 + 1 - 原始分
                value = scale.getOptionType() + 1 - value;
            }
            rawScore += value;
        }

        // 2. 计算标准分、等级与建议
        ScoreResult scoreResult = calcScore(scale, rawScore);

        // 3. 保存测评记录
        TestRecord record = new TestRecord();
        record.setUserId(userId);
        record.setScaleId(scaleId);
        record.setRawScore(rawScore);
        record.setStdScore(scoreResult.stdScore);
        record.setLevel(scoreResult.level);
        record.setSuggestion(scoreResult.suggestion);
        record.setStatus("1");
        recordMapper.insertRecord(record);

        // 4. 保存答题明细
        List<TestAnswer> answerList = new ArrayList<>();
        for (Question q : questions) {
            TestAnswer answer = new TestAnswer();
            answer.setRecordId(record.getRecordId());
            answer.setQuestionId(q.getQuestionId());
            answer.setOptionValue(answerMap.get(q.getQuestionId()));
            answerList.add(answer);
        }
        answerMapper.batchInsert(answerList);

        // 5. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("record", record);
        result.put("scaleName", scale.getScaleName());
        result.put("scaleCode", scale.getScaleCode());
        return result;
    }

    /**
     * 计分规则（各量表标准分与等级阈值）
     */
    private ScoreResult calcScore(Scale scale, int rawScore) {
        BigDecimal std;
        String level;
        String advice;
        String scaleCode = scale.getScaleCode();
        switch (scaleCode == null ? "" : scaleCode) {
            case "SAS":
                std = BigDecimal.valueOf(rawScore * 1.25).setScale(1, BigDecimal.ROUND_HALF_UP);
                if (std.compareTo(BigDecimal.valueOf(50)) < 0) {
                    level = "正常";
                    advice = "当前焦虑水平处于正常范围，请保持规律作息与积极心态，如有需要可定期复测。";
                } else if (std.compareTo(BigDecimal.valueOf(60)) < 0) {
                    level = "轻度焦虑";
                    advice = "存在轻度焦虑倾向。建议通过运动锻炼、呼吸放松、社交倾诉等方式自我调节，注意劳逸结合，必要时可寻求心理咨询支持。";
                } else if (std.compareTo(BigDecimal.valueOf(70)) < 0) {
                    level = "中度焦虑";
                    advice = "存在中度焦虑倾向。建议尽快前往心理门诊或精神心理科进行专业评估，可配合认知行为训练与专业心理治疗，避免长期处于高压状态。";
                } else {
                    level = "重度焦虑";
                    advice = "存在重度焦虑倾向。强烈建议尽快前往正规医疗机构心理科或精神科就诊，接受专业诊断与干预；也可拨打全国心理援助热线 12356 寻求即时支持。";
                }
                break;
            case "SDS":
                std = BigDecimal.valueOf(rawScore * 1.25).setScale(1, BigDecimal.ROUND_HALF_UP);
                if (std.compareTo(BigDecimal.valueOf(53)) < 0) {
                    level = "正常";
                    advice = "当前抑郁水平处于正常范围，请保持阳光心态与规律生活，多参与户外活动与社交，如有需要可定期复测。";
                } else if (std.compareTo(BigDecimal.valueOf(63)) < 0) {
                    level = "轻度抑郁";
                    advice = "存在轻度抑郁倾向。建议增加户外活动与日光照射，保持规律作息，多与亲友沟通倾诉，培养兴趣爱好以转移注意力。";
                } else if (std.compareTo(BigDecimal.valueOf(73)) < 0) {
                    level = "中度抑郁";
                    advice = "存在中度抑郁倾向。建议尽快前往心理科或精神科就诊，接受专业评估与系统治疗；请勿独自承受，可向信任的家人朋友寻求陪伴与支持。";
                } else {
                    level = "重度抑郁";
                    advice = "存在重度抑郁倾向。强烈建议立即前往正规医疗机构精神心理科就诊，接受专业诊断与治疗；如有自伤或轻生念头，请立即联系亲友陪同就医，或拨打全国心理援助热线 12356。";
                }
                break;
            case "SCL90":
                std = BigDecimal.valueOf(rawScore);
                BigDecimal avg = BigDecimal.valueOf(rawScore)
                        .divide(BigDecimal.valueOf(20), 2, BigDecimal.ROUND_HALF_UP);
                if (avg.compareTo(BigDecimal.valueOf(1.5)) < 0) {
                    level = "正常";
                    advice = "各项症状因子均分处于正常范围，心理健康状况良好，请继续保持健康的生活方式。";
                } else if (avg.compareTo(BigDecimal.valueOf(2.5)) < 0) {
                    level = "轻度症状";
                    advice = "存在轻度心理症状，可能反映近期压力或情绪波动。建议规律作息、适度运动、合理安排工作与休息，症状持续时可寻求心理咨询。";
                } else if (avg.compareTo(BigDecimal.valueOf(3.5)) < 0) {
                    level = "中度症状";
                    advice = "存在中度心理症状，涉及多个维度。建议尽快前往心理科或精神科进行全面评估，由专业人员制定干预方案。";
                } else {
                    level = "重度症状";
                    advice = "存在重度心理症状。强烈建议尽快前往正规医疗机构精神心理科就诊，接受系统诊断与治疗；紧急时可拨打全国心理援助热线 12356 求助。";
                }
                break;
            case "SRSS":
                std = BigDecimal.valueOf(rawScore);
                if (std.compareTo(BigDecimal.valueOf(40)) < 0) {
                    level = "正常";
                    advice = "睡眠状况总体良好，请继续保持规律作息，睡前减少电子产品使用，营造舒适的睡眠环境。";
                } else if (std.compareTo(BigDecimal.valueOf(60)) < 0) {
                    level = "轻度睡眠问题";
                    advice = "存在轻度睡眠问题。建议固定就寝与起床时间，睡前避免咖啡浓茶与剧烈运动，可通过温水泡脚、听轻音乐等方式助眠。";
                } else if (std.compareTo(BigDecimal.valueOf(80)) < 0) {
                    level = "中度睡眠问题";
                    advice = "存在中度睡眠问题。建议改善睡眠卫生习惯，白天适度运动，如持续失眠请到睡眠专科或心理科就诊，评估是否需要专业干预。";
                } else {
                    level = "重度睡眠问题";
                    advice = "存在重度睡眠问题。建议尽快前往医院睡眠医学科或心理精神科就诊，接受专业评估与治疗，避免长期依赖安眠药物。";
                }
                break;
            default:
                std = BigDecimal.valueOf(rawScore);
                level = "正常";
                advice = "测评完成，请结合自身情况关注心理健康。";
        }
        ScoreResult result = new ScoreResult();
        result.stdScore = std;
        result.level = level;
        result.suggestion = advice;
        return result;
    }

    /** 计分结果 */
    private static class ScoreResult {
        private BigDecimal stdScore;
        private String level;
        private String suggestion;
    }
}
