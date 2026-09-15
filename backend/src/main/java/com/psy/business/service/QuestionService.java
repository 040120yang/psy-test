package com.psy.business.service;

import com.psy.business.domain.Question;
import com.psy.common.core.TableDataInfo;

import java.util.List;

/**
 * 量表题目业务层
 */
public interface QuestionService {

    /** 分页查询题目列表（管理端） */
    TableDataInfo list(Question question);

    /** 根据量表ID查询全部题目（答题端） */
    List<Question> listByScale(Long scaleId);

    /** 新增题目 */
    int add(Question question);

    /** 修改题目 */
    int edit(Question question);

    /** 删除题目 */
    int remove(Long questionId);
}
