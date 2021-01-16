# zipper-table 拉链表
Manage changing data by zipper table

拉链表用于管理大量数据的历史变化状态

## 表结构

```
CREATE TABLE `zipper`.`zipper_table` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `business_id` bigint(20) NOT NULL COMMENT '业务id',
  `name` varchar(100) NOT NULL COMMENT '名称',
  `created` int(1) NOT NULL COMMENT '增加更新状态，1：增加，0：更新',
  `deleted` int(1) NOT NULL COMMENT '删除状态，1：已删除，0：未删除',
  `start_date` date NOT NULL COMMENT '生效时间',
  `end_date` date NOT NULL COMMENT '失效时间',
   PRIMARY KEY (`id`)
  );

CREATE TABLE `zipper`.`zipper_table_temp` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `name` varchar(100) NOT NULL COMMENT '名称',
  `sync_date` date NOT NULL COMMENT '同步时间',
   PRIMARY KEY (`id`)
  );
```
## 维护数据
1. 第一次全量插入所有数据
2. 每天增量维护：比较今天与昨天的数据，得到增加、更新和删除的数据
    1. 增加的数据，直接插入，生效日期为今天，失效日期为永久（9999-01-01）
    2. 删除的数据，失效日期为今天
    3. 更新的数据，失效日期为今天，同时插入一条数据，生效日期为今天，失效日期为永久

## 详细设计 
1. 增加三个字段：增加状态（created，0：更新，1：增加），删除状态（deleted：0：未删除，1：已删除）生效日期（start_date），失效日期（end_date）
2. 2020-01-01增加（开琏）：（1，0, 2020-01-01，9999-01-01）
3. 2020-01-02更新（闭链和开琏）：（1, 0, 2020-01-01，2020-01-02）,（0，0, 2020-01-02，9999-01-01）
4. 2020-01-03删除（闭链）：（0，1, 2020-01-02，2020-01-03）

## 使用场景
1. 查询当前存在的数据：end_date=9999-01-01
2. 查询2020-01-01存在的数据：start_date<=2020-01-01 and end_date>2020-01-01
3. 查询2020-01-01增加的数据：created=1 and start_date=2020-01-01
4. 查询2020-01-02更新的数据：created=0 and start_date=2020-01-02
5. 查询2020-01-03删除的数据：deleted=1 and end_date=2020-01-03
6. 查询2020-01-01~2020-01-10增加的数据：created=1 and start_date>=2020-01-01 and start_date<=2020-01-10
7. 查询各月增加且月底还有效的数量：
SELECT DATE_FORMAT(start_date, '%Y-%m') AS "t", COUNT(DISTINCT business_id) AS "c" 
FROM zipper 
WHERE end_date>last_day(start_date)
GROUP BY t

## 日常更新
1. 当天数据插入临时表
2. 关联昨天数据得到删除的数据
3. 关联昨天数据得到增加的数据
4. 关联昨天数据得到贡献的数据
