<!DOCTYPE html>
<html>
<!-- 我是一个注释 -->
<head>
    <meta charset="utf-8">
    <title>Hello World!</title>
</head>
<body>
    <#if stu.age gte 18 >
        ${stu.name} 成年了...
    </#if>
    <hr />
    <b>算数运算符</b>
    <br/><br/>
    100+5 运算：  ${100 + stu.age }<br/>
    100 - 5 * 5运算：${100 - 5 * 5}<br/>
    5 / 2运算：${5 / 2}<br/>
    12 % 10运算：${12 % 10}<br/>
    <hr>

    判断学生对象是否存在
    <#if stusssss??>
        打印一个 true
    </#if>

    <hr/>
    获取一个不存在得数据
    如果emp不存在那么就展示一个空字符串
    ${emp!''}
</body>
</html>