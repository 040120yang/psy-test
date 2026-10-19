/**
 * 时间显示格式化
 *
 * 后端实体字段为 java.util.Date，经全局 Jackson 配置序列化为 yyyy-MM-dd HH:mm:ss；
 * 聚合查询（Map）返回的字段为 LocalDateTime，序列化为 ISO 文本（2026-10-19T09:13:52）。
 * 展示层统一为前者，避免同一界面出现两种时间格式。
 */

/**
 * 格式化时间文本
 *
 * @param {*} value 原始值，可为 null
 * @returns {string} yyyy-MM-dd HH:mm:ss；空值返回 '-'
 */
export function formatTime(value) {
  if (value === null || value === undefined || value === '') return '-'
  const text = String(value).replace('T', ' ')
  return text.length > 19 ? text.slice(0, 19) : text
}

/**
 * el-table-column 的 formatter，按列绑定的字段取值
 *
 * @param {object} row 行数据
 * @param {object} column 列配置
 * @returns {string} 格式化后的时间
 */
export function timeColumnFormatter(row, column) {
  return formatTime(row[column.property])
}
