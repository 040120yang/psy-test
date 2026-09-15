package com.psy.business.service.impl;

import com.psy.business.domain.Question;
import com.psy.business.domain.Scale;
import com.psy.business.mapper.QuestionMapper;
import com.psy.business.mapper.ScaleMapper;
import com.psy.business.service.QuestionService;
import com.psy.common.core.PageUtils;
import com.psy.common.core.TableDataInfo;
import com.psy.common.exception.ServiceException;
import com.psy.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 量表题目业务实现
 */
@Service
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private ScaleMapper scaleMapper;

    @Override
    public TableDataInfo list(Question question) {
        PageUtils.startPage();
        List<Question> list = questionMapper.selectQuestionList(question);
        return PageUtils.getDataTable(list);
    }

    @Override
    public List<Question> listByScale(Long scaleId) {
        return questionMapper.selectByScaleId(scaleId);
    }

    @Override
    public int add(Question question) {
        validate(question);
        return questionMapper.insertQuestion(question);
    }

    @Override
    public int edit(Question question) {
        if (question.getQuestionId() == null) {
            throw new ServiceException("题目ID不能为空");
        }
        validate(question);
        return questionMapper.updateQuestion(question);
    }

    @Override
    public int remove(Long questionId) {
        return questionMapper.deleteQuestionById(questionId);
    }

    private void validate(Question question) {
        if (question.getScaleId() == null) {
            throw new ServiceException("请选择所属量表");
        }
        Scale scale = scaleMapper.selectScaleById(question.getScaleId());
        if (scale == null) {
            throw new ServiceException("所属量表不存在");
        }
        if (StringUtils.isEmpty(question.getContent())) {
            throw new ServiceException("题目内容不能为空");
        }
        if (question.getSortNo() == null) {
            question.setSortNo(0);
        }
        if (question.getReverseFlag() == null) {
            question.setReverseFlag(0);
        }
    }
}
