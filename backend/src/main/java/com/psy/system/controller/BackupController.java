package com.psy.system.controller;

import com.psy.common.core.AjaxResult;
import com.psy.common.exception.ServiceException;
import com.psy.framework.aspectj.annotation.OperLog;
import com.psy.framework.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 数据备份与导出（A-07，仅系统管理员）。
 * 支持：表清单、单表 CSV 导出、整库打包 zip 导出、恢复指引。
 * 表名经白名单正则 + information_schema 存在性双重校验，防止 SQL 注入。
 */
@RestController
@RequestMapping("/system/backup")
public class BackupController {

    /** 表名白名单：仅字母、数字、下划线 */
    private static final Pattern TABLE_NAME = Pattern.compile("^[A-Za-z0-9_]{1,64}$");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    /** 表清单（表名、行数、数据大小、备注） */
    @GetMapping("/tables")
    public AjaxResult tables() {
        requireAdmin();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT t.TABLE_NAME AS tableName, t.TABLE_ROWS AS tableRows, "
                        + "ROUND(t.DATA_LENGTH/1024, 1) AS dataKb, t.TABLE_COMMENT AS tableComment "
                        + "FROM information_schema.TABLES t WHERE t.TABLE_SCHEMA = ? ORDER BY t.TABLE_NAME",
                currentSchema());
        return AjaxResult.success(rows);
    }

    /** 单表导出为 CSV */
    @OperLog(title = "数据备份-单表导出", businessType = OperLog.OTHER)
    @GetMapping("/exportTable")
    public void exportTable(@RequestParam String table, HttpServletResponse response) throws Exception {
        requireAdmin();
        validateTable(table);
        response.setContentType("text/csv;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + table + ".csv");
        java.io.Writer writer = response.getWriter();
        writer.write(csvOf(table));
        writer.flush();
    }

    /** 整库打包导出（每张表一个 CSV，压缩为 zip） */
    @OperLog(title = "数据备份-整库导出", businessType = OperLog.OTHER)
    @GetMapping("/exportAll")
    public void exportAll(HttpServletResponse response) throws Exception {
        requireAdmin();
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = ? ORDER BY TABLE_NAME",
                String.class, currentSchema());
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment;filename=" + currentSchema() + "_backup.zip");
        OutputStream os = response.getOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(os)) {
            for (String table : tables) {
                zos.putNextEntry(new ZipEntry(table + ".csv"));
                zos.write(csvOf(table).getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
        }
    }

    /** 恢复指引 */
    @GetMapping("/guide")
    public AjaxResult guide() {
        requireAdmin();
        Map<String, Object> info = new HashMap<>();
        info.put("database", currentSchema());
        info.put("csvNote", "导出的 CSV 均带 UTF-8 BOM，首行为列名，便于 Excel 直接打开");
        info.put("restoreSteps", new String[]{
                "1. 解压「整库导出」得到的 zip，内含每张表一个 CSV 文件；",
                "2. 使用 MySQL 客户端（Navicat / 命令行）将 CSV 导入目标库的同名表；",
                "3. 命令行示例：LOAD DATA LOCAL INFILE 'table.csv' INTO TABLE <库名>.<表名> "
                        + "FIELDS TERMINATED BY ',' ENCLOSED BY '\"' LINES TERMINATED BY '\\n' IGNORE 1 LINES;",
                "4. 建议先导入测试库验证结果，确认无误后再切换生产库。"
        });
        return AjaxResult.success(info);
    }

    /** 生成单表 CSV 文本（含 BOM，首行列名） */
    private String csvOf(String table) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM `" + table + "`");
        StringBuilder sb = new StringBuilder();
        sb.append('\ufeff');
        if (!rows.isEmpty()) {
            List<String> cols = new ArrayList<>(rows.get(0).keySet());
            for (int i = 0; i < cols.size(); i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(csvCell(cols.get(i)));
            }
            sb.append('\n');
            for (Map<String, Object> row : rows) {
                for (int i = 0; i < cols.size(); i++) {
                    if (i > 0) {
                        sb.append(',');
                    }
                    Object v = row.get(cols.get(i));
                    sb.append(csvCell(v == null ? "" : String.valueOf(v)));
                }
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    /** 从数据源 URL 解析当前库名 */
    private String currentSchema() {
        try {
            String path = datasourceUrl;
            int q = path.indexOf('?');
            if (q >= 0) {
                path = path.substring(0, q);
            }
            return path.substring(path.lastIndexOf('/') + 1);
        } catch (Exception e) {
            return "psy_test";
        }
    }

    /** 表名校验：正则白名单 + 存在性核对 */
    private void validateTable(String table) {
        if (table == null || !TABLE_NAME.matcher(table).matches()) {
            throw new ServiceException("非法的表名");
        }
        Integer cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM information_schema.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?",
                Integer.class, currentSchema(), table);
        if (cnt == null || cnt == 0) {
            throw new ServiceException("指定的表不存在");
        }
    }

    private String csvCell(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void requireAdmin() {
        if (!SecurityUtils.isAdmin()) {
            throw new ServiceException("无权限操作，仅系统管理员可管理数据备份");
        }
    }
}
