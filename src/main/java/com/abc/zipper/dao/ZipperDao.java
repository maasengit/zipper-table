package com.abc.zipper.dao;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public class ZipperDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static String FOR_EVER_DATE = "9999-01-01";

    public void create(Long businessId, String name, Date date) {
        String d = DateFormatUtils.format(date,"yyyy-MM-dd");
        String sql = "INSERT INTO `zipper`.`zipper_table` (`business_id`,`name`,`created`,`deleted`,`start_date`,`end_date`) " +
                "VALUES (?, ?, 1, 0, ?, ?)";
        jdbcTemplate.update(sql, businessId, name, d, FOR_EVER_DATE);
    }

    public void update(Long businessId, String name, Date date) {
        String d = DateFormatUtils.format(date,"yyyy-MM-dd");
        String sql1 = "UPDATE `zipper`.`zipper_table` SET `end_date` = '" + d + "' WHERE `business_id` = " + businessId;
        String sql2 = "INSERT INTO `zipper`.`zipper_table` (`business_id`,`name`,`created`,`deleted`,`start_date`,`end_date`) "
                + "VALUES (" + businessId + ", '" + name + "', 0, 0, '" + d + "', '" + FOR_EVER_DATE + "')";
        jdbcTemplate.batchUpdate(sql1, sql2);
    }

    public void delete(Long businessId, Date date) {
        String d = DateFormatUtils.format(date,"yyyy-MM-dd");
        String sql = "UPDATE `zipper`.`zipper_table` SET `deleted`=1, `end_date` = '" + d
                + "' WHERE `business_id` = " + businessId;
        jdbcTemplate.update(sql);
    }

    public List<Map<String, Object>> findAll() {
        String sql = "SELECT * FROM `zipper`.`zipper_table` WHERE `end_date` = ?";
        return jdbcTemplate.queryForList(sql, FOR_EVER_DATE);
    }

    public List<Map<String, Object>> findAll(Date date) {
        String d = DateFormatUtils.format(date,"yyyy-MM-dd");
        String sql = "SELECT * FROM `zipper`.`zipper_table` WHERE `start_date` <= ? AND `end_date` > ?";
        return jdbcTemplate.queryForList(sql, d, d);
    }

    public List<Map<String, Object>> findCreated(Date date) {
        String d = DateFormatUtils.format(date,"yyyy-MM-dd");
        String sql = "SELECT * FROM `zipper`.`zipper_table` WHERE `created`=1 AND `start_date` = ?";
        return jdbcTemplate.queryForList(sql, d);
    }

    public List<Map<String, Object>> findCreated(Date fromDate, Date toDate) {
        String fromD = DateFormatUtils.format(fromDate,"yyyy-MM-dd");
        String toD = DateFormatUtils.format(toDate,"yyyy-MM-dd");
        String sql = "SELECT * FROM `zipper`.`zipper_table` WHERE `created`=1 AND `start_date` BETWEEN ? AND ?";
        return jdbcTemplate.queryForList(sql, fromD, toD);
    }

    public List<Map<String, Object>> findDeleted(Date date) {
        String d = DateFormatUtils.format(date,"yyyy-MM-dd");
        String sql = "SELECT * FROM `zipper`.`zipper_table` WHERE `deleted`=1 AND `end_date` = ?";
        return jdbcTemplate.queryForList(sql, d);
    }

    public List<Map<String, Object>> findUpdated(Date date) {
        String d = DateFormatUtils.format(date,"yyyy-MM-dd");
        String sql = "SELECT * FROM `zipper`.`zipper_table` WHERE `created`=0 AND `start_date` = ?";
        return jdbcTemplate.queryForList(sql, d);
    }
}
