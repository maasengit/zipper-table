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

    /**
     * 新增一条数据
     *
     * @param businessId
     * @param name
     * @param date
     */
    public void create(Long businessId, String name, Date date) {
        String d = DateFormatUtils.format(date, "yyyy-MM-dd");
        String sql = "INSERT INTO `zipper`.`zipper_table` (`business_id`,`name`,`created`,`deleted`,`start_date`,`end_date`) " +
                "VALUES (?, ?, 1, 0, ?, ?)";
        jdbcTemplate.update(sql, businessId, name, d, FOR_EVER_DATE);
    }

    /**
     * 更新一条数据
     *
     * @param businessId
     * @param oldName
     * @param newName
     * @param date
     */
    public void update(Long businessId, String oldName, String newName, Date date) {
        String d = DateFormatUtils.format(date, "yyyy-MM-dd");
        String sql1 = "UPDATE `zipper`.`zipper_table` SET `end_date` = '" + d + "' WHERE `business_id` = " + businessId
                + " AND `name` = '" + oldName + "'";
        String sql2 = "INSERT INTO `zipper`.`zipper_table` (`business_id`,`name`,`created`,`deleted`,`start_date`,`end_date`) "
                + "VALUES (" + businessId + ", '" + newName + "', 0, 0, '" + d + "', '" + FOR_EVER_DATE + "')";
        jdbcTemplate.batchUpdate(sql1, sql2);
    }

    /**
     * 删除一条数据
     *
     * @param businessId
     * @param date
     */
    public void delete(Long businessId, String name, Date date) {
        String d = DateFormatUtils.format(date, "yyyy-MM-dd");
        String sql = "UPDATE `zipper`.`zipper_table` SET `deleted`=1, `end_date` = ? WHERE `business_id` = ? " +
                "AND `name` = ? AND `deleted`=0";
        jdbcTemplate.update(sql, d, businessId, name);
    }

    /**
     * 查询当前有效数据
     *
     * @return
     */
    public List<Map<String, Object>> findAllValid() {
        String sql = "SELECT * FROM `zipper`.`zipper_table` WHERE `end_date` = ?";
        return jdbcTemplate.queryForList(sql, FOR_EVER_DATE);
    }

    /**
     * 查询某天有效数据
     *
     * @param date
     * @return
     */
    public List<Map<String, Object>> findAllValid(Date date) {
        String d = DateFormatUtils.format(date, "yyyy-MM-dd");
        String sql = "SELECT * FROM `zipper`.`zipper_table` WHERE `start_date` <= ? AND `end_date` > ?";
        return jdbcTemplate.queryForList(sql, d, d);
    }

    /**
     * 查询某天增加的数据
     *
     * @param date
     * @return
     */
    public List<Map<String, Object>> findByCreatedDate(Date date) {
        String d = DateFormatUtils.format(date, "yyyy-MM-dd");
        String sql = "SELECT * FROM `zipper`.`zipper_table` WHERE `created`=1 AND `start_date` = ?";
        return jdbcTemplate.queryForList(sql, d);
    }

    /**
     * 查询日期范围增加的数据
     *
     * @param fromDate
     * @param toDate
     * @return
     */
    public List<Map<String, Object>> findByCreatedDate(Date fromDate, Date toDate) {
        String fromD = DateFormatUtils.format(fromDate, "yyyy-MM-dd");
        String toD = DateFormatUtils.format(toDate, "yyyy-MM-dd");
        String sql = "SELECT * FROM `zipper`.`zipper_table` WHERE `created`=1 AND `start_date` BETWEEN ? AND ?";
        return jdbcTemplate.queryForList(sql, fromD, toD);
    }

    /**
     * 查询某天删除的数据
     *
     * @param date
     * @return
     */
    public List<Map<String, Object>> findByDeletedDate(Date date) {
        String d = DateFormatUtils.format(date, "yyyy-MM-dd");
        String sql = "SELECT * FROM `zipper`.`zipper_table` WHERE `deleted`=1 AND `end_date` = ?";
        return jdbcTemplate.queryForList(sql, d);
    }

    /**
     * 查询某天更新的数据
     *
     * @param date
     * @return
     */
    public List<Map<String, Object>> findByUpdatedDate(Date date) {
        String d = DateFormatUtils.format(date, "yyyy-MM-dd");
        String sql = "SELECT * FROM `zipper`.`zipper_table` WHERE `created`=0 AND `start_date` = ?";
        return jdbcTemplate.queryForList(sql, d);
    }

    /**
     * 查询某条记录的变化情况
     * 
     * @param businessId
     * @return
     */
    public List<Map<String, Object>> findAllHistory(Long businessId) {
        String sql = "SELECT * FROM `zipper`.`zipper_table` WHERE `business_id`=?";
        return jdbcTemplate.queryForList(sql, businessId);
    }

    /**
     * 查询某条记录的增加历史
     * @param businessId
     * @return
     */
    public List<Map<String, Object>> findCreatedHistory(Long businessId) {
        String sql = "SELECT * FROM `zipper`.`zipper_table` WHERE `created`=1 and `business_id`=? ORDER BY start_date ASC";
        return jdbcTemplate.queryForList(sql, businessId);
    }

    /**
     * 查询某条记录的更新历史
     * @param businessId
     * @return
     */
    public List<Map<String, Object>> findUpdatedHistory(Long businessId) {
        String sql = "SELECT * FROM `zipper`.`zipper_table` WHERE `created`=0 and `business_id`=? ORDER BY start_date ASC";
        return jdbcTemplate.queryForList(sql, businessId);
    }

    /**
     * 查询某条记录的删除历史
     * @param businessId
     * @return
     */
    public List<Map<String, Object>> findDeletedHistory(Long businessId) {
        String sql = "SELECT * FROM `zipper`.`zipper_table` WHERE `deleted`=1 and `business_id`=? ORDER BY start_date ASC";
        return jdbcTemplate.queryForList(sql, businessId);
    }

    /**
     * 比较得到增加的数据
     * @return
     */
    public List<Map<String, Object>> diffCreated() {
        String sql = "SELECT temp.id AS business_id, temp.name, temp.sync_date FROM `zipper`.`zipper_table` AS zipper RIGHT JOIN `zipper`.`zipper_table_temp` AS temp " +
                "ON zipper.business_id=temp.id AND zipper.name=temp.name WHERE zipper.business_id IS NULL";
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * 比较得到删除的数据
     * @return
     */
    public List<Map<String, Object>> diffDeleted() {
        String sql = "SELECT zipper.business_id, zipper.name FROM `zipper`.`zipper_table` AS zipper LEFT JOIN `zipper`.`zipper_table_temp` AS temp " +
                "ON zipper.business_id=temp.id AND zipper.name=temp.name WHERE temp.id IS NULL";
        return jdbcTemplate.queryForList(sql);
    }
}
