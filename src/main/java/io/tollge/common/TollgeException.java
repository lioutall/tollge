package io.tollge.common;

import lombok.Getter;

/**
 * 定义异常
 *
 * @author toyer
 * @date 2018-09-21
 */
@Getter
public class TollgeException extends RuntimeException {
    private StatusCodeMsg s;
    public TollgeException() {
        super();
    }
    public TollgeException(String message) {
        super(message);
    }
    public TollgeException(String message, Throwable cause) {
        super(message, cause);
    }
    public TollgeException(Throwable cause) {
        super(cause);
    }

    public TollgeException(StatusCodeMsg s) {
        super(s.getMsg());
        this.s = s;
    }

    public TollgeException(StatusCodeMsg s, String message) {
        super(message);
        this.s = s;
    }

    public TollgeException(StatusCodeMsg s, Throwable cause) {
        super(cause);
        this.s = s;
    }
}
