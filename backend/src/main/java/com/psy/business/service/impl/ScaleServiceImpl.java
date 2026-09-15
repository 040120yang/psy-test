package com.psy.business.service.impl;

import com.psy.business.domain.Scale;
import com.psy.business.mapper.QuestionMapper;
import com.psy.business.mapper.ScaleMapper;
import com.psy.business.service.ScaleService;
import com.psy.common.core.PageUtils;
import com.psy.common.core.TableDataInfo;
import com.psy.common.exception.ServiceException;
import com.psy.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 量表业务实现
 */
@Service
public class ScaleServiceImpl implements ScaleService {

    @Autowired
    private ScaleMapper scaleMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Override
    public TableDataInfo list(Scale scale) {
        PageUtils.startPage();
        List<Scale> list = scaleMapper.selectScaleList(scale);
        return PageUtils.getDataTable(list);
    }

    @Override
    public List<Scale> listEnabled() {
        Scale query = new Scale();
        query.setStatus("0");
        return scaleMapper.selectScaleList(query);
    }

    @Override
    public int add(Scale scale) {
        if (StringUtils.isEmpty(scale.getScaleName()) || StringUtils.isEmpty(scale.getScaleCode())) {
            throw new ServiceException("量表名称与编码不能为空");
        }
        if (scaleMapper.countByCode(scale.getScaleCode(), 0L) > 0) {
            throw new ServiceException("量表编码已存在");
        }
        if (scale.getOptionType() == null) {
            scale.setOptionType(4);
        }
        if (StringUtils.isEmpty(scale.getStatus())) {
            scale.setStatus("0");
        }
        return scaleMapper.insertScale(scale);
    }

    @Override
    public int edit(Scale scale) {
        if (scale.getScaleId() == null) {
            throw new ServiceException("量表ID不能为空");
        }
        Scale exist = scaleMapper.selectScaleById(scale.getScaleId());
        if (exist == null) {
            throw new ServiceException("量表不存在");
        }
        if (StringUtils.isNotEmpty(scale.getScaleCode()) && scaleMapper.countByCode(scale.getScaleCode(), scale.getScaleId()) > 0) {
            throw new ServiceException("量表编码已存在");
        }
        return scaleMapper.updateScale(scale);
    }

    @Override
    public int remove(Long scaleId) {
        Scale exist = scaleMapper.selectScaleById(scaleId);
        if (exist == null) {
            throw new ServiceException("量表不存在");
        }
        // 删除量表及其题目
        questionMapper.deleteByScaleId(scaleId);
        return scaleMapper.deleteScaleById(scaleId);
    }
}
