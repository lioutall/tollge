package com.tollge.common;

import java.util.ArrayList;
import java.util.List;

public class Page<E> extends BaseModel {
    private static final long serialVersionUID = 1L;

    /** 页码，从1开始     */
    private int pageNum;
    /** 页面大小     */
    private int pageSize;
    /** 起始行     */
    private int startRow;
    /** 末行     */
    private int endRow;
    /** 总数     */
    private long total;
    /** 总页数     */
    private int pages;

    private List<E> result = new ArrayList<>();

    public Page(){

    }

    public Page(int pageNum, int pageSize) {
        if (pageNum == 1 && pageSize == Integer.MAX_VALUE) {
            pageSize = 0;
        }
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        calculateStartAndEndRow();
    }

    public List<E> getResult() {
        return this.result;
    }

    public void setResult(List<E> result) {
        this.result = result;
    }

    public int getPages() {
        return pages;
    }

    public Page<E> setPages(int pages) {
        this.pages = pages;
        return this;
    }

    public int getEndRow() {
        return endRow;
    }

    public Page<E> setEndRow(int endRow) {
        this.endRow = endRow;
        return this;
    }

    public int getPageNum() {
        return pageNum;
    }

    public Page<E> setPageNum(int pageNum) {
        //分页合理化，针对不合理的页码自动处理
        this.pageNum = pageNum <= 0 ? 1 : pageNum;
        return this;
    }

    public int getPageSize() {
        return pageSize;
    }

    public Page<E> setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public int getStartRow() {
        return startRow;
    }

    public Page<E> setStartRow(int startRow) {
        this.startRow = startRow;
        return this;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
        if (total == -1) {
            pages = 1;
            return;
        }
        if (pageSize > 0) {
            pages = (int) (total / pageSize + ((total % pageSize == 0) ? 0 : 1));
        } else {
            pages = 0;
        }
        //分页合理化，针对不合理的页码自动处理
        if (pageNum > pages) {
            if(pages!=0){
                pageNum = pages;
            }
            calculateStartAndEndRow();
        }
    }

    /**
     * 计算起止行号
     */
    private void calculateStartAndEndRow() {
        this.startRow = this.pageNum > 0 ? (this.pageNum - 1) * this.pageSize : 0;
        this.endRow = this.startRow + this.pageSize * (this.pageNum > 0 ? 1 : 0);
    }

    /**
     * 设置页码
     *
     * @param pageNum
     * @return
     */
    public Page<E> pageNum(int pageNum) {
        //分页合理化，针对不合理的页码自动处理
        this.pageNum = (pageNum <= 0) ? 1 : pageNum;
        return this;
    }

    /**
     * 设置页面大小
     *
     * @param pageSize
     * @return
     */
    public Page<E> pageSize(int pageSize) {
        this.pageSize = pageSize;
        calculateStartAndEndRow();
        return this;
    }


}
