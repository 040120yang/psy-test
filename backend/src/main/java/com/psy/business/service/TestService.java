package com.psy.business.service;

import com.psy.business.domain.TestAnswer;
import com.psy.business.domain.TestRecord;

import java.util.List;
import java.util.Map;

/**
 * 心理测评业务层
 */
public interface TestService {

    /** 提交答卷，返回测评结果（记录+明细+报告） */
    Map<String, Object> submit(Long userId, Long scaleId, List<AnswerItem> answers);

    /** 答题项 */
    class AnswerItem {
        private Long questionId;
        private Integer optionValue;

        public Long getQuestionId() {
            return questionId;
        }

        public void setQuestionId(Long questionId) {
            this.questionId = questionId;
        }

        public Integer getOptionValue() {
            return optionValue;
        }

        public void setOptionValue(Integer optionValue) {
            this.optionValue = optionValue;
        }
    }

    /** 测评结果封装 */
    class TestResult {
        private TestRecord record;
        private List<TestAnswer> answers;

        public TestRecord getRecord() {
            return record;
        }

        public void setRecord(TestRecord record) {
            this.record = record;
        }

        public List<TestAnswer> getAnswers() {
            return answers;
        }

        public void setAnswers(List<TestAnswer> answers) {
            this.answers = answers;
        }
    }
}
