package com.heima.wemedia;

import com.heima.common.aliyun.GreenImageScan;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.file.service.FileStorageService;
import com.heima.wemedia.service.WmNewsAutoScanService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Map;

@SpringBootTest(classes = WemediaApplication.class)
@RunWith(SpringRunner.class)
public class AliyunTest {

    @Autowired
    private GreenTextScan greenTextScan; //检验文本是否合规

    @Autowired
    private GreenImageScan greenImageScan; //检验图片是否合规

    @Autowired
    private FileStorageService fileStorageService; //MiniO

    /**
     * 如果文本有问题，返回值
     * 如果没有问题，  返回值
     * @throws Exception
     */
    @Test
    public  void testScanText() throws Exception {
        Map map = greenTextScan.greeTextScan("私人侦探、针孔摄象、信用卡提现、广告代理、代开发票、刻章办、出售答案、小额贷款…");
        System.err.println("map----:"+map);
    }

    @Test
    public void testScanImage() throws Exception {
        byte[] bytes = fileStorageService.downLoadFile("http://192.168.200.130:9000/leadnews/2022/02/13/cc43044085984b76ac31616cd8dcc5da.jpg");
        Map map = greenImageScan.imageScan(Arrays.asList(bytes));
        System.out.println(map);
    }

    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;

    @Test
    public void test3(){
        wmNewsAutoScanService.autoScanWmNews(6235);
    }

    @Test
    public void test4() throws Exception {
        //2S + 3S = 5S  缩短这个时间   3S  多线程
        AliyunTest aliyunTest = new AliyunTest();
        aliyunTest.testScanText();  //请求所需事件为2S中     aliyunTest.testScanImage(); //请求所需事件为3S中  交给子线程


    }
}