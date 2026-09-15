package com.psy.business.mapper;

import com.psy.business.domain.Scale;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

/**
 * 量表数据层
 */
@Mapper
public interface ScaleMapper {

    /** 分页/条件查询量表列表（含题目数量） */
    List<Scale> selectScaleList(@Param("scale") Scale scale);

    /** 根据ID查询量表 */
    @Select("select * from psy_scale where scale_id = #{scaleId}")
    Scale selectScaleById(@Param("scaleId") Long scaleId);

    /** 新增量表 */
    int insertScale(Scale scale);

    /** 修改量表 */
    int updateScale(Scale scale);

    /** 删除量表 */
    @Delete("delete from psy_scale where scale_id = #{scaleId}")
    int deleteScaleById(@Param("scaleId") Long scaleId);

    /** 校验编码唯一 */
    @Select("select count(1) from psy_scale where scale_code = #{scaleCode} and scale_id != #{scaleId}")
    int countByCode(@Param("scaleCode") String scaleCode, @Param("scaleId") Long scaleId);
}
