package com.alibaba.datax.plugin.reader.excelreader;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.datax.common.element.BoolColumn;
import com.alibaba.datax.common.element.Column;
import com.alibaba.datax.common.element.DateColumn;
import com.alibaba.datax.common.element.DoubleColumn;
import com.alibaba.datax.common.element.LongColumn;
import com.alibaba.datax.common.element.Record;
import com.alibaba.datax.common.element.StringColumn;
import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.plugin.RecordSender;
import com.alibaba.datax.plugin.unstructuredstorage.reader.ColumnEntry;
import com.alibaba.datax.plugin.unstructuredstorage.reader.UnstructuredStorageReaderErrorCode;

public class ExcelRowReader {

	private static final Logger LOG = LoggerFactory.getLogger(ExcelRowReader.class);
	private RecordSender recordSender;

	private List<ColumnEntry> columnConfigs;

	private Record record;

	private Column columnGenerated;

	private enum Type {
		STRING, LONG, BOOLEAN, DOUBLE, DATE,;
	}

	public ExcelRowReader(RecordSender recordSender, List<ColumnEntry> columnConfigs) {
		this.recordSender = recordSender;
		this.columnConfigs = columnConfigs;
	}

	public void transportOneRecord(Object object) {
		record = recordSender.createRecord();
		ArrayList<String> data = (ArrayList<String>) object;
		int configSize = columnConfigs.size();

		for (ColumnEntry columnConfig : columnConfigs) {
			String columnType = columnConfig.getType();
			Integer columnIndex = columnConfig.getIndex();
			String columnConst = columnConfig.getValue();
			String columnValue = "";
			if (null == columnIndex && null == columnConst) {
				throw DataXException.asDataXException(UnstructuredStorageReaderErrorCode.NO_INDEX_VALUE,
						"由于您配置了type, 则至少需要配置 index 或 value");
			}

			if (null != columnIndex) {
				if (columnIndex >= data.size()) {
					// 处理越界的情况 Excel 特殊处理。全部处理为空
					columnGenerated = new StringColumn("");

				} else {
					columnValue = data.get(columnIndex);
				}

			} else {
				columnValue = columnConst;
			}

			Type type = Type.valueOf(columnType.toUpperCase());

			switch (type) {
			case STRING:
				columnGenerated = new StringColumn(columnValue);
				break;
			case LONG:
				try {

					if (columnValue == null || "".equals(columnValue)) {
						columnGenerated = new StringColumn(columnValue);
					} else {
						columnGenerated = new LongColumn(columnValue);
					}
				} catch (Exception e) {
					throw new IllegalArgumentException(String.format("类型转换错误, 无法将[%s] 转换为[%s]", columnValue, "LONG"));
				}
				break;
			case DOUBLE:
				try {
					if (columnValue == null || "".equals(columnValue)) {
						columnGenerated = new StringColumn(columnValue);
					} else {
						columnGenerated = new DoubleColumn(columnValue);
					}
				} catch (Exception e) {
					throw new IllegalArgumentException(String.format("类型转换错误, 无法将[%s] 转换为[%s]", columnValue, "DOUBLE"));
				}
				break;
			case BOOLEAN:
				try {
					if (columnValue == null || "".equals(columnValue)) {
						columnGenerated = new StringColumn(columnValue);
					} else {
						columnGenerated = new BoolColumn(columnValue);
					}
				} catch (Exception e) {
					throw new IllegalArgumentException(
							String.format("类型转换错误, 无法将[%s] 转换为[%s]", columnValue, "BOOLEAN"));
				}

				break;
			case DATE:
				// System.out.println();
				try {
					// if (columnValue == null || "".equals(columnValue)) {
					// columnGenerated = new StringColumn(columnValue);
					// } else {
					if (columnValue == null || "".equals(columnValue)) {
						Date date = null;
						String formatString = columnConfig.getFormat();
						if (StringUtils.isNoneBlank(formatString)) {
							date = new Date();
							DateFormat format = columnConfig.getDateFormat();
							String currentDate = format.format(date);
							columnGenerated = new StringColumn(currentDate);

						} else {
							columnGenerated = new DateColumn(date);
						}

					} else {
						String formatString = columnConfig.getFormat();
						// if (null != formatString) {
						if (StringUtils.isNotBlank(formatString)) {
							// 用户自己配置的格式转换, 脏数据行为出现变化
							DateFormat format = columnConfig.getDateFormat();
							Date parse = format.parse(columnValue);

							columnGenerated = new DateColumn(parse);
						} else {
							// 框架尝试转换
							columnGenerated = new DateColumn(new StringColumn(columnValue).asDate());
						}
					}
					// }

				} catch (Exception e) {
					throw new IllegalArgumentException(String.format("类型转换错误, 无法将[%s] 转换为[%s]", columnValue, "DATE"));
				}
				break;
			default:
				String errorMessage = String.format("您配置的列类型暂不支持 : [%s]", columnType);
				LOG.error(errorMessage);
				throw DataXException.asDataXException(UnstructuredStorageReaderErrorCode.NOT_SUPPORT_TYPE,
						errorMessage);
			}
			record.addColumn(columnGenerated);

		}
		recordSender.sendToWriter(record);
	}

}
