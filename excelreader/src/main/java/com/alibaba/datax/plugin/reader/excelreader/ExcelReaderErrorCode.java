package com.alibaba.datax.plugin.reader.excelreader;

import com.alibaba.datax.common.spi.ErrorCode;

/**
 * Created by haiwei.luo on 14-9-20.
 */
public enum ExcelReaderErrorCode implements ErrorCode {
	REQUIRED_VALUE("ExcelReader-00", "您缺失了必须填写的参数值."),
	ILLEGAL_VALUE("ExcelReader-01", "您填写的参数值不合法."),
	MIXED_INDEX_VALUE("ExcelReader-02", "您的列信息配置同时包含了index,value."),
	NO_INDEX_VALUE("ExcelReader-03","您明确的配置列信息,但未填写相应的index,value."),
	
	FILE_NOT_EXISTS("ExcelReader-04", "您配置的目录文件路径不存在或者没有权限读取."),
	OPEN_FILE_WITH_CHARSET_ERROR("ExcelReader-05", "您配置的文件编码和实际文件编码不符合."),
	OPEN_FILE_ERROR("ExcelReader-06", "您配置的文件在打开时异常."),
	READ_FILE_IO_ERROR("ExcelReader-07", "您配置的文件在读取时出现IO异常."),
	SECURITY_NOT_ENOUGH("ExcelReader-08", "您缺少权限执行相应的文件操作."),
	CONFIG_INVALID_EXCEPTION("ExcelReader-09", "您的参数配置错误."),
	RUNTIME_EXCEPTION("ExcelReader-10", "出现运行时异常, 请联系我们"),
	EMPTY_DIR_EXCEPTION("ExcelReader-11", "您尝试读取的文件目录为空."),	
	
	FAIL_LOGIN("ExcelReader-12", "登录失败,无法与ftp服务器建立连接."),
	FAIL_DISCONNECT("ExcelReader-13", "关闭ftp连接失败,无法与ftp服务器断开连接."),
	COMMAND_FTP_IO_EXCEPTION("ExcelReader-14", "与ftp服务器连接异常."),
	OUT_MAX_DIRECTORY_LEVEL("ExcelReader-15", "超出允许的最大目录层数."),
	LINK_FILE("ExcelReader-16", "您尝试读取的文件为链接文件."),;

	private final String code;
	private final String description;

	private ExcelReaderErrorCode(String code, String description) {
		this.code = code;
		this.description = description;
	}

	@Override
	public String getCode() {
		return this.code;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	@Override
	public String toString() {
		return String.format("Code:[%s], Description:[%s].", this.code,
				this.description);
	}
}
