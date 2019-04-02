package com.alibaba.datax.plugin.reader.excelreader;

import java.nio.charset.UnsupportedCharsetException;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.plugin.unstructuredstorage.reader.Key;
import com.alibaba.datax.plugin.unstructuredstorage.reader.UnstructuredStorageReaderErrorCode;

public class ExcelParseCheck {
	
	public static void validateParameter(Configuration readerConfiguration) {

		// encoding check
		validateEncoding(readerConfiguration);

		//only support compress types
		validateCompress(readerConfiguration);

	}

	public static void validateEncoding(Configuration readerConfiguration) {
		// encoding check
		String encoding = readerConfiguration
				.getString(
						com.alibaba.datax.plugin.unstructuredstorage.reader.Key.ENCODING,
						com.alibaba.datax.plugin.unstructuredstorage.reader.Constant.DEFAULT_ENCODING);
		try {
			encoding = encoding.trim();
			readerConfiguration.set(Key.ENCODING, encoding);
			Charsets.toCharset(encoding);
		} catch (UnsupportedCharsetException uce) {
			throw DataXException.asDataXException(UnstructuredStorageReaderErrorCode.ILLEGAL_VALUE,
					String.format("不支持您配置的编码格式 : [%s]", encoding), uce);
		} catch (Exception e) {
			throw DataXException.asDataXException(UnstructuredStorageReaderErrorCode.CONFIG_INVALID_EXCEPTION,
					String.format("编码配置异常, 请联系我们: %s", e.getMessage()), e);
		}
	}

	public static void validateCompress(Configuration readerConfiguration) {
		String compress =readerConfiguration
				.getUnnecessaryValue(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.COMPRESS,null,null);
		if(StringUtils.isNotBlank(compress)){
			compress = compress.toLowerCase().trim();
			boolean compressTag = "gzip".equals(compress) || "bzip2".equals(compress) || "zip".equals(compress)
					|| "lzo".equals(compress) || "lzo_deflate".equals(compress) || "hadoop-snappy".equals(compress)
					|| "framing-snappy".equals(compress);
			if (!compressTag) {
				throw DataXException.asDataXException(UnstructuredStorageReaderErrorCode.ILLEGAL_VALUE,
						String.format("仅支持 gzip, bzip2, zip, lzo, lzo_deflate, hadoop-snappy, framing-snappy " +
								"文件压缩格式, 不支持您配置的文件压缩格式: [%s]", compress));
			}
		}else{
			// 用户可能配置的是 compress:"",空字符串,需要将compress设置为null
			compress = null;
		}
		readerConfiguration.set(com.alibaba.datax.plugin.unstructuredstorage.reader.Key.COMPRESS, compress);
	}
}
