package com.tollge.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageRequest extends BaseModel {

    private int pageNum;
    private int pageSize = 10;

}
