package com.psy.business.service;

import com.psy.business.domain.TestAnswer;
import com.psy.business.domain.TestRecord;
import com.psy.common.core.TableDataInfo;

import java.util.List;
import java.util.Map;

/**
 * 测评记录业务层
 */
public interface RecordService {

    /** 当前用户的测评记录（可按量表名称/量表筛选） */
    TableDataInfo myRecords(TestRecord record);

    /** 全部测评记录（管理端，可按条件筛选） */
    TableDataInfo allRecords(TestRecord record);

    /** 记录详情（含答题明细） */
    Map<String, Object> detail(Long recordId);

    /** 获取当前用户有权查看的测评记录 */
    TestRecord getAuthorizedRecord(Long recordId);

    /** 删除记录 */
    int remove(Long recordId);
}
