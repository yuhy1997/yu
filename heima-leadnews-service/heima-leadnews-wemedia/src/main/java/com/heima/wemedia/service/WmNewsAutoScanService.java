package com.heima.wemedia.service;

public interface WmNewsAutoScanService {

    /**
     * 自动审核接口
     * id 需要审核的文章ID
     */
    public void autoScanWmNews(Integer id);

    /**
     * 多线程执行方法
     */
    void test2();
}
