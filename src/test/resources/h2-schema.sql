CREATE SCHEMA IF NOT EXISTS zipper;
CREATE TABLE `zipper`.`zipper-table` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `business_id` bigint(20) NOT NULL COMMENT '业务id',
  `created` int(1) NOT NULL COMMENT '增加更新状态，1：增加，0：更新',
  `deleted` int(1) NOT NULL COMMENT '删除状态，1：已删除，0：未删除',
  `start_date` date NOT NULL COMMENT '生效时间',
  `end_date` date NOT NULL COMMENT '失效时间',
   PRIMARY KEY (`id`)
  );