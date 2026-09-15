package com.psy.business.service.impl;

import com.psy.business.domain.TestAnswer;
import com.psy.business.domain.TestRecord;
import com.psy.business.mapper.TestAnswerMapper;
import com.psy.business.mapper.TestRecordMapper;
import com.psy.business.service.RecordService;
import com.psy.common.core.PageUtils;
import com.psy.common.core.TableDataInfo;
import com.psy.common.exception.ServiceException;
import com.psy.framework.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测评记录业务实现
 */
@Service
public class RecordServiceImpl implements RecordService {

    @Autowired
    private TestRecordMapper recordMapper;

    @Autowired
    private TestAnswerMapper answerMapper;

    @Override
    public TableDataInfo myRecords() {
        Long userId = SecurityUtils.getUserId();
        PageUtils.startPage();
        List<TestRecord> list = recordMapper.selectMyRecords(userId);
        return PageUtils.getDataTable(list);
    }

    @Override
    public TableDataInfo allRecords(TestRecord record) {
        if (!SecurityUtils.isAdmin() && !SecurityUtils.isDoctor()) {
            throw new ServiceException("无权限查看全部测评记录");
        }
        PageUtils.startPage();
        List<TestRecord> list = recordMapper.selectAllRecords(record);
        return PageUtils.getDataTable(list);
    }

    @Override
    public Map<String, Object> detail(Long recordId) {
        TestRecord record = recordMapper.selectRecordById(recordId);
        if (record == null) {
            throw new ServiceException("测评记录不存在");
        }
        // 权限：公众用户只能查看自己的记录
        if (SecurityUtils.isUser() && !SecurityUtils.getUserId().equals(record.getUserId())) {
            throw new ServiceException("无权限查看该记录");
        }
        List<TestAnswer> answers = answerMapper.selectByRecordId(recordId);
        Map<String, Object> result = new HashMap<>();
        result.put("record", record);
        result.put("answers", answers);
        return result;
    }

    @Override
    public int remove(Long recordId) {
        TestRecord record = recordMapper.selectRecordById(recordId);
        if (record == null) {
            throw new ServiceException("测评记录不存在");
        }
        if (SecurityUtils.isUser() && !SecurityUtils.getUserId().equals(record.getUserId())) {
            throw new ServiceException("无权限删除该记录");
        }
        return recordMapper.deleteRecordById(recordId);
    }
}
