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

    @Override
    public String exportCsv(Long scaleId) {
        List<Question> questions = questionMapper.selectByScaleId(scaleId);
        StringBuilder sb = new StringBuilder();
        // 表头（加 BOM 便于 Excel 识别 UTF-8）
        sb.append('\ufeff').append("题号,题目内容,反向计分").append('\n');
        if (questions != null) {
            for (Question q : questions) {
                sb.append(q.getSortNo() == null ? "" : q.getSortNo()).append(',')
                        .append(csvCell(q.getContent())).append(',')
                        .append(q.getReverseFlag() == null ? 0 : q.getReverseFlag())
                        .append('\n');
            }
        }
        return sb.toString();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public int importCsv(Long scaleId, String csvText, boolean overwrite) {
        Scale scale = scaleMapper.selectScaleById(scaleId);
        if (scale == null) {
            throw new ServiceException("所属量表不存在");
        }
        if (StringUtils.isEmpty(csvText)) {
            throw new ServiceException("导入内容为空");
        }
        java.util.List<String[]> rows = parseCsv(csvText);
        if (rows.isEmpty()) {
            throw new ServiceException("未解析到有效题目数据");
        }
        if (overwrite) {
            questionMapper.deleteByScaleId(scaleId);
        }
        int count = 0;
        for (String[] cols : rows) {
            Question q = new Question();
            q.setScaleId(scaleId);
            // 列：题号,题目内容,反向计分
            Integer sortNo = null;
            if (cols.length > 0 && StringUtils.isNotEmpty(cols[0])) {
                try {
                    sortNo = Integer.valueOf(cols[0].trim());
                } catch (NumberFormatException ignored) {
                }
            }
            q.setSortNo(sortNo == null ? count + 1 : sortNo);
            q.setContent(cols.length > 1 ? cols[1] : "");
            Integer reverseFlag = 0;
            if (cols.length > 2 && StringUtils.isNotEmpty(cols[2])) {
                String rf = cols[2].trim();
                reverseFlag = ("1".equals(rf) || "是".equals(rf) || "true".equalsIgnoreCase(rf)) ? 1 : 0;
            }
            q.setReverseFlag(reverseFlag);
            if (StringUtils.isEmpty(q.getContent())) {
                continue;
            }
            questionMapper.insertQuestion(q);
            count++;
        }
        if (count == 0) {
            throw new ServiceException("导入数据无效：无有效题目内容");
        }
        return count;
    }

    /** 解析 CSV 文本为行列（支持引号包裹与逗号转义），自动跳过表头 */
    private java.util.List<String[]> parseCsv(String text) {
        java.util.List<String[]> result = new java.util.ArrayList<>();
        java.util.List<String> row = new java.util.ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        boolean fieldStarted = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    field.append(c);
                }
            } else if (c == '"' && !fieldStarted) {
                inQuotes = true;
                fieldStarted = true;
            } else if (c == ',') {
                row.add(field.toString());
                field.setLength(0);
                fieldStarted = false;
            } else if (c == '\r') {
                // 忽略，配合 \n 处理换行
            } else if (c == '\n') {
                row.add(field.toString());
                field.setLength(0);
                fieldStarted = false;
                result.add(row.toArray(new String[0]));
                row = new java.util.ArrayList<>();
            } else {
                field.append(c);
                fieldStarted = true;
            }
        }
        if (fieldStarted || field.length() > 0) {
            row.add(field.toString());
        }
        if (!row.isEmpty()) {
            result.add(row.toArray(new String[0]));
        }
        // 去掉表头（首行包含「题号」或「题目内容」）
        if (!result.isEmpty()) {
            String[] head = result.get(0);
            if (head.length > 0 && head[0] != null && (head[0].contains("题号") || head[0].contains("题目"))) {
                result.remove(0);
            }
        }
        return result;
    }

    /** CSV 字段转义 */
    private String csvCell(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
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
