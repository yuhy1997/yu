package com.heima.minio.test;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;

import java.io.FileInputStream;

public class MinIOTest {

    //文件的上传
    public static void main(String[] args) {
        FileInputStream fileInputStream = null;
        try {
            //读取本地需要上传的文件
            fileInputStream =  new FileInputStream("D:\\hsr.jpg");;

            //1.创建minio链接客户端
            MinioClient minioClient = MinioClient.builder().credentials("minio", "minio123").endpoint("http://192.168.200.130:9000").build();

            //2.上传
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .object("hsr.jpg")//文件名
                    .contentType("text/jpge")//文件类型
                    .bucket("leadnews")//桶名词  与minio创建的名词一致
                    .stream(fileInputStream, fileInputStream.available(), -1) //文件流
                    .build();
            minioClient.putObject(putObjectArgs);
            System.out.println("上窜成功!!!!");
            //System.out.println("http://192.168.200.130:9000/leadnews/ak47.jpg");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}