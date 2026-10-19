package com.psy.business.service;

import com.psy.business.domain.FollowUp;
import com.psy.common.core.TableDataInfo;

/**
 * 随访业务层
 */
public interface FollowUpService {

    /** 分页查询随访列表（管理端，可按状态筛选） */
    TableDataInfo adminList(String status);

    /** 查询当前登录学生的随访列表 */
    TableDataInfo myList();

    /** 根据主键查询随访详情 */
    FollowUp getById(Long id);

    /** 新增随访，并向学生发送随访提醒 */
    int add(FollowUp followUp);

    /** 修改随访（「已逾期」为派生状态，提交时归一为「待随访」） */
    int update(FollowUp followUp);

    /** 完成随访 */
    int finish(Long id, String doctorNote, Integer symptomScore, String nextFollowDate);

    /** 删除随访 */
    int delete(Long id);

    /** 查询逾期随访列表 */
    TableDataInfo overdue();
}
