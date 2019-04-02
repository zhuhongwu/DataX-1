package com.alibaba.datax.plugin.reader.excelreader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.datax.common.element.Column;
import com.alibaba.datax.common.element.Record;
import com.alibaba.datax.common.element.StringColumn;
import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.plugin.RecordSender;
import com.alibaba.datax.common.plugin.TaskPluginCollector;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.plugin.reader.excelreader.listen.ExcelListener;
import com.alibaba.datax.plugin.unstructuredstorage.reader.ColumnEntry;
import com.alibaba.datax.plugin.unstructuredstorage.reader.UnstructuredStorageReaderErrorCode;
import com.alibaba.datax.plugin.unstructuredstorage.reader.UnstructuredStorageReaderUtil;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class ExcelParserHelper {

	private static final Logger LOG = LoggerFactory.getLogger(ExcelParserHelper.class);

	public ExcelParserHelper() {
	}

	public static void readFromStream(InputStream inputStream, String context, String sheetIndex,
			Configuration readerSliceConfig, RecordSender recordSender, TaskPluginCollector taskPluginCollector) {

		String encoding = readerSliceConfig.getString(Key.ENCODING, Constant.DEFAULT_ENCODING);
		// handle blank encoding
		if (StringUtils.isBlank(encoding)) {
			encoding = Constant.DEFAULT_ENCODING;
			LOG.warn(String.format("您配置的encoding为[%s], 使用默认值[%s]", encoding, Constant.DEFAULT_ENCODING));
		}

		// 获取列值字段
		List<Configuration> column = readerSliceConfig
				.getListConfiguration(com.alibaba.datax.plugin.reader.excelreader.Key.HEADER);
		// handle ["*"] -> [], null
		if (null != column && 1 == column.size() && "\"*\"".equals(column.get(0).toString())) {
			readerSliceConfig.set(com.alibaba.datax.plugin.reader.excelreader.Key.HEADER, null);
			column = null;
		}

		List<ColumnEntry> columns = UnstructuredStorageReaderUtil.getListColumnEntry(readerSliceConfig,
				com.alibaba.datax.plugin.reader.excelreader.Key.HEADER);

		// warn: no default value '\N'
		String nullFormat = readerSliceConfig.getString(Key.NULL_FORMAT);

		// excel header
		Integer skipHeader;

		// deal data
		try {
			// 读取指定的sheet页面

			Integer sheetNo = null;

			if (context.endsWith(Constant.EXCEL03_EXTENSION)) {
				sheetNo = readerSliceConfig.getInt(Key.SHEET, Constant.DEFAULT_SHEET_INDEX + 1);

			} else if (context.endsWith(Constant.EXCEL07_EXTENSION)) {
				sheetNo = readerSliceConfig.getInt(Key.SHEET, Constant.DEFAULT_SHEET_INDEX);

			} else {
				throw new Exception("文件格式错误，fileName的扩展名只能是xls或xlsx。");
			}
			// 跳过多少行
			skipHeader = readerSliceConfig.getInt(Key.SKIP_HEADER, Constant.DEFAULT_SKIPHEADER);

			ExcelListener excelListener = new ExcelListener();
			excelListener.setExcelRowReader(new ExcelRowReader(recordSender, columns));

			Sheet sheet = new Sheet(sheetNo, skipHeader);
			EasyExcelFactory.readBySax(inputStream, sheet, excelListener);

		} catch (NullPointerException e) {
			throw DataXException.asDataXException(UnstructuredStorageReaderErrorCode.RUNTIME_EXCEPTION,
					"运行时错误, 请联系我们--------------", e);
		} catch (Exception e) {
			throw DataXException.asDataXException(UnstructuredStorageReaderErrorCode.RUNTIME_EXCEPTION,
					"运行时错误, 请联系我们--------------", e);
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				throw DataXException.asDataXException(UnstructuredStorageReaderErrorCode.RUNTIME_EXCEPTION, "io操作异常",
						e);
			}
		}

	}

	public static Record transportOneRecord(RecordSender recordSender, String[] sourceLine, String nullFormat,
			TaskPluginCollector taskPluginCollector) {
		Record record = recordSender.createRecord();
		Column columnGenerated = null;

		// 创建都为String类型column的record
		for (String columnValue : sourceLine) {
			// not equalsIgnoreCase, it's all ok if nullFormat is null
			if (columnValue.equals(nullFormat)) {
				columnGenerated = new StringColumn(null);
			} else {
				columnGenerated = new StringColumn(columnValue);
			}
			record.addColumn(columnGenerated);
		}
		recordSender.sendToWriter(record);

		return record;
	}

	public static List<ColumnEntry> getListColumnEntry(Configuration configuration, final String path) {
		List<JSONObject> lists = configuration.getList(path, JSONObject.class);
		if (lists == null) {
			return null;
		}
		List<ColumnEntry> result = new ArrayList<ColumnEntry>();
		for (final JSONObject object : lists) {
			result.add(JSON.parseObject(object.toJSONString(), ColumnEntry.class));
		}
		return result;
	}

}
