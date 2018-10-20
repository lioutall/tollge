package com.tollge.common;

import io.vertx.core.eventbus.ReplyException;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 通用全局统一异常处理
 *
 * @author Mirren
 *
 */
@Slf4j
public class UFailureHandler {

	/**
	 * 当router发生异常时统一使用该方法处理
	 *
	 * @param rct []
	 */
	public static void unifiedFail(RoutingContext rct) {
		String result;
		if (rct.failure() instanceof NullPointerException) {
			result = ResultFormat.formatAsNull(StatusCodeMsg.C412);
		} else if (rct.failure() instanceof RuntimeException) {
			result = ResultFormat.formatAsNull(StatusCodeMsg.C500);
		} else {
			result = ResultFormat.formatAsNull(StatusCodeMsg.C1500);
		}
		rct.response().end(result);
	}

	/**
	 * 根据错误状态码返回相应的StatusCodeMsg
	 *
	 * @param code []
	 * @return []
	 */
	static StatusCodeMsg asStatus(int code) {
		return StatusCodeMsg.getErrorCode(code);
	}

	public static String commonFailure(Throwable cause) {
		if (cause instanceof ReplyException) {
			ReplyException re = (ReplyException) cause;
			int code = re.failureCode();
			StatusCodeMsg asStatus = asStatus(code);
			return ResultFormat.formatError(asStatus, cause.getMessage());
		} else if(cause instanceof IllegalArgumentException) {
			return ResultFormat.formatError(StatusCodeMsg.C414, cause.getMessage());
		} else {
			log.error("unknown exceptions:", cause);
			return ResultFormat.formatError(StatusCodeMsg.C412, cause.getMessage());
		}
	}

}
