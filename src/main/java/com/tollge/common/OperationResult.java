package com.tollge.common;

import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * for db operation return type
 * @param <E>
 */
@NoArgsConstructor
public class OperationResult<E> extends ArrayList<E> {
    private static final long serialVersionUID = 1L;

    private int countRow;

    public int getCountRow() {
        return countRow;
    }

    public void setCountRow(int countRow) {
        this.countRow = countRow;
    }
}
