package com.alibaba.datax.plugin.reader.excelreader.listen;

import com.alibaba.datax.plugin.reader.excelreader.ExcelRowReader;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

public class ExcelListener extends AnalysisEventListener {

	private ExcelRowReader excelRowReader;

	public void setExcelRowReader(ExcelRowReader excelRowReader) {
		this.excelRowReader = excelRowReader;
	}

	@Override
	public void invoke(Object object, AnalysisContext context) {
		excelRowReader.transportOneRecord(object);
	}

	@Override
	public void doAfterAllAnalysed(AnalysisContext context) {

	}

}
