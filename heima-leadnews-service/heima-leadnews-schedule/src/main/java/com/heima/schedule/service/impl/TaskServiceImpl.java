package com.heima.schedule.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.common.constants.ScheduleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.schedule.pojos.Taskinfo;
import com.heima.model.schedule.pojos.TaskinfoLogs;
import com.heima.schedule.mapper.TaskinfoLogsMapper;
import com.heima.schedule.mapper.TaskinfoMapper;
import com.heima.schedule.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class TaskServiceImpl implements TaskService {


    @Autowired
    private CacheService cacheService;

    /**
     * 添加任务方法
     * @param task   任务对象
     * @return
     */
    @Override
    public long addTask(Task task) {
        //1.把任务数据保存到mysql 中
        boolean flag = addTaskToDb(task);
        if(!flag){ //代表存储到数据库失败
            return 0;
        }

        //创建一个存储的key
        String key = task.getTaskType() + "_" + task.getPriority();

        //3.如果执行时间 > = 当前时间  把任务交给未来数据队列 预加载5分钟的
        //获取5分钟之后的时间  毫秒值
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        long nextScheduleTime = calendar.getTimeInMillis();

        //2.如果执行时间<当前时间 把任务交给LIST消费队列  把任务数据分配给任务队列
        if(task.getExecuteTime() < System.currentTimeMillis()){
            //添加到立即消费队列
            cacheService.lLeftPush(ScheduleConstants.TOPIC + key, JSON.toJSONString(task));
        }else if( task.getExecuteTime() <=  nextScheduleTime ){
            //添加到未来队列
            cacheService.zAdd(ScheduleConstants.FUTURE + key,JSON.toJSONString(task),task.getExecuteTime());
        }
        return task.getTaskId();
    }
    @Autowired
    private TaskinfoMapper taskinfoMapper;

    @Autowired
    private TaskinfoLogsMapper taskinfoLogsMapper;
    //任务数据保存到数据库中
    private boolean addTaskToDb(Task task) {
        try {
            //1.把task对象转换成taskinfo 对象
            Taskinfo taskinfo = new Taskinfo();
            BeanUtils.copyProperties(task,taskinfo);
            //1.1 补全一下时间
            taskinfo.setExecuteTime(new Date(task.getExecuteTime()));
            //2.存储到任务表中即可
            taskinfoMapper.insert(taskinfo);
            //3.添加任务日志到日志表中
            TaskinfoLogs taskinfoLogs = new TaskinfoLogs();
            BeanUtils.copyProperties(taskinfo,taskinfoLogs);
            taskinfoLogs.setStatus(0);//设置任务的初始状态
            taskinfoLogsMapper.insert(taskinfoLogs);
            //4.补全task id的值
            task.setTaskId(taskinfo.getTaskId());
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 取消任务
     * @param taskId
     * @return
     */
    @Override
    public boolean cancelTask(long taskId) {
        boolean flag = false;
        //删除任务，更新日志
        Task task = updateDb(taskId,ScheduleConstants.CANCELLED);

        //删除redis的数据
        if(task != null){
            //优先级和任务类型
            removeTaskFromCache(task);
            flag = true;
        }
        return flag;
    }

    /**
     * 消费任务接口
     * @param type 任务类型
     * @param priority  任务的优先级
     *                  redis key 就是通过这两个值来进行创建
     * @return
     */
    @Override
    public Task poll(int type, int priority) {
        //1.创建redis key
        String key = type + "_" + priority;

        //2.消费redis 队列中的数据即可   右取
        String s = cacheService.lRightPop(ScheduleConstants.TOPIC + key);

        //3.转换成对象
        if(StringUtils.isNotEmpty(s)){
            Task task = JSON.parseObject(s,Task.class);

            //4.把任务日志表中的信息修改成一执行 1
            TaskinfoLogs taskinfoLogs = new TaskinfoLogs();
            taskinfoLogs.setTaskId(task.getTaskId());
            taskinfoLogs.setStatus(ScheduleConstants.EXECUTED);
            //5.更新数据中
            taskinfoLogsMapper.updateById(taskinfoLogs);
            return task;
        }
        return null;
    }

    //删除redis 数据
    private void removeTaskFromCache(Task task) {

        //1.获得redis key
        //创建一个存储的key
        String key = task.getTaskType() + "_" + task.getPriority();

        //2.如果执行时间<当前时间 把任务交给LIST消费队列  把任务数据分配给任务队列
        if(task.getExecuteTime() < System.currentTimeMillis()){
            //删除到立即消费队列
            cacheService.lRemove(ScheduleConstants.TOPIC + key,0,JSON.toJSONString(task));
        }
        //3.如果执行时间 > = 当前时间  把任务交给未来数据队列 预加载5分钟的
        //获取5分钟之后的时间  毫秒值
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        long nextScheduleTime = calendar.getTimeInMillis();
        if(task.getExecuteTime() <=  nextScheduleTime  ){
            //删除到未来队列
            cacheService.zRemove(ScheduleConstants.FUTURE + key,JSON.toJSONString(task));
        }

    }

    //删除任务，并且修改任务日志
    private Task updateDb(long taskId, int executed) {
        //0.先查询出这个任务
        Taskinfo taskinfo = taskinfoMapper.selectById(taskId);

        //1.删除任务数据
        taskinfoMapper.deleteById(taskId);

        //2.修改任务日志数据
        TaskinfoLogs taskinfoLogs = new TaskinfoLogs();
        taskinfoLogs.setTaskId(taskId);
        //0 初始化 ，1.已经执行 2.取消
        taskinfoLogs.setStatus(ScheduleConstants.CANCELLED);
        //3.修改日志数据库
        taskinfoLogsMapper.updateById(taskinfoLogs);

        Task task = new Task();
        BeanUtils.copyProperties(taskinfo,task);
        //task时间手动set
        task.setExecuteTime(taskinfo.getExecuteTime().getTime());
        return task;
    }


    //定时任务
    //Spring task 任务调度框架 （定时器）
    //cron 表达式
    //BUG  单线程
    /**
     * 第一个   秒
     * 第二个   分
     * 第三个   小时
     * 第四天   日
     * 第五个   月
     * 第六个   星期几     2099年
     * * 代表所有时间
     * ？ 代表不取值
     * 日和周 必须要有一个是？
     *
     * 1.每天早上9.30点名   0 30 9 * * ？
     *                     0 1 * * * ?
     *   每分钟的0秒执行一次
     *
     */
    // "0 */1 * * * ?
    @Scheduled(cron = "0 */1 * * * ? ")
    public void refresh() {
        String token = cacheService.tryLock("FUTURE_TASK_SYNC", 1000 * 30);
        if(StringUtils.isNotBlank(token)){
            //1.从未来队列中取出数据
            //2.把取出的数据存储到消费队列
            // 获取所有未来数据集合的key值
            Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");// future_*
            //把所有未来队列的数据放入到消费队列
            for (String futureKey : futureKeys) { // future_250_250
                //ScheduleConstants.TOPIC + futureKey.split(ScheduleConstants.FUTURE)[1] 得到 优先级和任务类型
                String topicKey = ScheduleConstants.TOPIC + futureKey.split(ScheduleConstants.FUTURE)[1];
                //获取该组key下当前需要消费的任务数据
                Set<String> tasks = cacheService.zRangeByScore(futureKey, 0, System.currentTimeMillis());
                if (!tasks.isEmpty()) {
                    //将这些任务数据添加到消费者队列中
                    cacheService.refreshWithPipeline(futureKey, topicKey, tasks);
                    System.out.println("成功的将" + futureKey + "下的当前需要执行的任务数据刷新到" + topicKey + "下");
                }
            }
        }
    }


    @Scheduled(cron = "0 */5 * * * ?")
    @PostConstruct
    public void reloadData() {
        clearCache();
        log.info("数据库数据同步到缓存");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);

        //查看小于未来5分钟的所有任务
        List<Taskinfo> allTasks = taskinfoMapper.selectList(Wrappers.<Taskinfo>lambdaQuery().lt(Taskinfo::getExecuteTime,calendar.getTime()));
        if(allTasks != null && allTasks.size() > 0){
            for (Taskinfo taskinfo : allTasks) {
                Task task = new Task();
                BeanUtils.copyProperties(taskinfo,task);
                task.setExecuteTime(taskinfo.getExecuteTime().getTime());
                addTask(task);
            }
        }
    }

    private void clearCache(){
        // 删除缓存中未来数据集合和当前消费者队列的所有key
        Set<String> futurekeys = cacheService.scan(ScheduleConstants.FUTURE + "*");// future_
        Set<String> topickeys = cacheService.scan(ScheduleConstants.TOPIC + "*");// topic_
        cacheService.delete(futurekeys);
        cacheService.delete(topickeys);
    }

}
