package com.heima.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.dtos.UserSearchDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.search.service.ApUserSearchService;
import com.heima.search.service.ArticleSearchService;
import com.heima.utils.thread.ApUserThreadLocalUtil;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ArticleSearchServiceImpl implements ArticleSearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    //搜索记录，是搜索成功后添加呢？ 只要搜索我就添加
    @Override
    public ResponseResult search(UserSearchDto dto) throws IOException {
        //1.检查参数
        if(dto == null || StringUtils.isBlank(dto.getSearchWords())){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2.设置查询条件
        //查询哪个数据表  app_info_article
        SearchRequest searchRequest = new SearchRequest("app_info_article");
        //设置查询条件的 wrapper
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); //
        //布尔查询（封装多个查询条件）  进行多条件查询的封装
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //根据搜索关键词来进行查询   游戏 手机  and   or  先分词后匹配
        QueryStringQueryBuilder queryBuilder = QueryBuilders.queryStringQuery(dto.getSearchWords()).field("title").field("content").defaultOperator(Operator.OR);
        //把条件天骄  boolQueryBuilder
        boolQueryBuilder.filter(queryBuilder); //must 匹配数据一摸一样，  filter过滤数据
        //时间查询
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("publishTime").lt(dto.getMinBehotTime().getTime());
        //添加 boolQueryBuilder 第二个条件
        boolQueryBuilder.filter(rangeQueryBuilder);
        //分页查询
        searchSourceBuilder.from(0); //从第几条开始
        searchSourceBuilder.size(dto.getPageSize()); //显示多少条

        //按照发布时间倒序查询
        searchSourceBuilder.sort("publishTime", SortOrder.DESC);

        //高亮查询
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.preTags("<font style='color: red; font-size: inherit;'>");
        highlightBuilder.postTags("</font>");
        searchSourceBuilder.highlighter(highlightBuilder);
        //保存条件到wrapper
        searchSourceBuilder.query(boolQueryBuilder); //查询条件的集合
        //把wrapper 放入进行参数中
        searchRequest.source(searchSourceBuilder);

        //restHighLevelClient    更新  发送请求查询出所有的数据
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        //3.结果封装返回
        List<Map> list = new ArrayList<>();

        SearchHit[] hits = searchResponse.getHits().getHits();
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
            Map map = JSON.parseObject(json, Map.class);
            //处理高亮
            if(hit.getHighlightFields() != null && hit.getHighlightFields().size() > 0){
                Text[] titles = hit.getHighlightFields().get("title").getFragments();
                String title = StringUtils.join(titles);
                //高亮标题
                map.put("h_title",title);
            }else {
                //原始标题
                map.put("h_title",map.get("title"));
            }
            list.add(map);
        }

        ApUser user = ApUserThreadLocalUtil.getUser();

        if(user != null){//不存在该用户
            //todo 异步调用历史记录新增接口
            apUserSearchService.insert(dto.getSearchWords(),user.getId());
        }

        return ResponseResult.okResult(list);
    }

    @Autowired
    private ApUserSearchService apUserSearchService;
}
