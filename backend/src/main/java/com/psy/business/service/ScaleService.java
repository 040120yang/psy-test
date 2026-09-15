package com.psy.business.service;

import com.psy.business.domain.Scale;
import com.psy.common.core.TableDataInfo;

import java.util.List;

/**
 * 量表业务层
 */
public interface ScaleService {

    /** 分页查询量表列表（管理端） */
    TableDataInfo list(Scale scale);

    /** 查询全部启用量表（答题端） */
    List<Scale> listEnabled();

    /** 新增量表 */
    int add(Scale scale);

    /** 修改量表 */
    int edit(Scale scale);

    /** 删除量表（连同题目） */
    int remove(Long scaleId);
}
