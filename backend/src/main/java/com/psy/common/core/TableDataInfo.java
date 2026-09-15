package com.psy.common.core;

import java.io.Serializable;
import java.util.List;

/**
 * 表格分页数据对象（参照若依 TableDataInfo）
 */
public class TableDataInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 总记录数 */
    private long total;

    /** 列表数据 */
    private List<?> rows;

    /** 消息状态码 */
    private int code;

    /** 消息内容 */
    private String msg;

    public TableDataInfo() {
    }

    public TableDataInfo(List<?> list, long total) {
        this.rows = list;
        this.total = total;
    }

    public static TableDataInfo success(List<?> list, long total) {
        TableDataInfo rspData = new TableDataInfo(list, total);
        rspData.setCode(200);
        rspData.setMsg("查询成功");
        return rspData;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<?> getRows() {
        return rows;
    }

    public void setRows(List<?> rows) {
        this.rows = rows;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
