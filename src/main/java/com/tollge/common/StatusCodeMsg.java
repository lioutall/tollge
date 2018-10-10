package com.tollge.common;

/**
 * 状态码与状态信息的枚举类
 * 
 * @author Mirren
 *
 */
public enum StatusCodeMsg {
	/**
	 * 状态码
	 */
	C200(0,"成功"),
	C201(201,"操作正常但没有任何改变"),
	C202(202,"部分成功,部分失败"),
	
	C300(300,"用户未登陆"),
	C301(301,"被封禁的用户"),
	C302(302,"用户未注册"),
	C303(303,"用户已注册"),
	C304(304,"用户名或密码错误"),
	C305(305,"手机未注册"),
	C306(306,"手机已注册"),
	C307(307,"验证码发送失败"),
	C308(308,"验证码失效"),
	C309(309,"验证码错误"),
	C310(310,"短信平台异常"),
	C313(313,"未授权或者权限过期"),
	C314(314,"非法操作或没有权限"),
	C315(315,"IP被限制"),
	C316(316,"授权异常"),

	C404(404,"无效的URL请求,或者指定URL不存在"),
	C406(406,"对象只能读取"),
	C407(407,"对象不存在"),
	C408(408,"请求超时"),
	
	C412(412,"缺少参数或者参数空值"),
	C413(413,"参数值不符合规定范围"),
	C414(414,"参数不合法"),
	C416(416,"频繁操作"),
	C431(431,"HTTP重复请求"),

	C500(500,"服务器内部错误"),
	C501(501, "数据获取失败"),

	// 服务器错误
	C1000(1000,"[服务器]运行时异常"),
	C1001(1001,"[服务器]空值异常"),
	C1002(1002,"[服务器]数据类型转换异常"),
	C1003(1003,"[服务器]IO异常"),
	C1004(1004,"[服务器]未知方法异常"),
	C1005(1005,"[服务器]数组越界异常"),
	C1006(1006,"[服务器]网络异常"),
	
	C1400(1400,"[服务器]Bad Request"),
	C1401(1401,"[服务器]NotAuthorization"),
	C1405(1405,"[服务器]Method Not Allowed 检查请求方式是否有错"),
	C1406(1406,"[服务器]Not Acceptable"),
	
	C1500(1500,"[服务器]Internal Server Error"),
	C1505(1505,"[服务器]缓存异常"),
	;
	// 状态码
	private int code;
	private String msg;

	StatusCodeMsg(int code, String msg) {
		this.msg = msg;
		this.code = code;
	}

	/**
	 * 得到状态码
	 * 
	 * @return
	 */
	public int getCode() {
		return code;
	}

	/**
	 * 获得状态码相应的信息
	 * 
	 * @return
	 */
	public String getMsg() {
		return msg;
	}

	public static StatusCodeMsg getErrorCode(int code) {
		for (StatusCodeMsg value : values()) {
			if(value.code == code) {
				return value;
			}
		}
		return StatusCodeMsg.C1500;
	}

}
