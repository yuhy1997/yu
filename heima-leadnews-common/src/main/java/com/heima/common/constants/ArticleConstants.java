package com.heima.common.constants;

public class ArticleConstants {

    //常量
    //0.查询默认
    public static final Short LOADTYPE_LOAD = 0;
    //1.查询更多
    public static final Short LOADTYPE_LOAD_MORE = 1;
    //2.查询最新
    public static final Short LOADTYPE_LOAD_NEW = 2;
    //3. 默认查询首页得所有分类
    public static final String DEFAULT_TAG = "__all__";
    //4.每页显示条数
    public static final Integer DEFAULT_SIZE=10;

    //1.阅读分值 1
    //2.点赞分值 3
    public static final Integer HOT_ARTICLE_LIKE_WEIGHT = 3;
    //3.评论分值 5
    public static final Integer HOT_ARTICLE_COMMENT_WEIGHT = 5;
    //4.收藏分支 8
    public static final Integer HOT_ARTICLE_COLLECTION_WEIGHT = 8;

    //热点文章redis 前缀
    public static final String HOT_ARTICLE_PAGE = "hot_article_page_";


}
