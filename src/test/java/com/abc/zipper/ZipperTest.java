package com.abc.zipper;

import com.abc.zipper.dao.ZipperDao;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class ZipperTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZipperTest.class);

    @Autowired
    private ZipperDao dao;

    @Test
    public void test() {
        List<Map<String, Object>> all = dao.findAllValid();
        // 最初10行
        assertEquals(10, all.size());
        LOGGER.info("最初的数据：");
        print(all);

        // 2020-01-02增加11，更新1，删除2
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2020);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 2);
        dao.create(11L, "张三", cal.getTime());

        List<Map<String, Object>> created = dao.findByCreatedDate(cal.getTime());
        assertEquals(1, created.size());
        assertEquals(11L, created.get(0).get("business_id"));
        assertEquals(ZipperDao.FOR_EVER_DATE, DateFormatUtils.format((Date) created.get(0).get("end_date"), "yyyy-MM-dd"));
        LOGGER.info("2020-01-02增加的数据: ");
        print(created);
        all = dao.findAllValid();
        // 现在11行
        assertEquals(11, all.size());

        // 更新1
        dao.update(1L, "张三1", "李四", cal.getTime());
        List<Map<String, Object>> updated = dao.findByUpdatedDate(cal.getTime());
        assertEquals(1L, updated.size());
        assertEquals("李四", updated.get(0).get("name"));

        LOGGER.info("2020-01-02更新的数据: ");
        print(updated);

        all = dao.findAllValid();
        // 现在11行
        assertEquals(11, all.size());
        // 删除2
        dao.delete(2L, "张三2", cal.getTime());
        dao.delete(1L, "李四", cal.getTime());
        all = dao.findAllValid();
        // 现在10行
        assertEquals(9, all.size());
        print(all);

        cal.set(Calendar.DAY_OF_MONTH, 1);
        all = dao.findAllValid(cal.getTime());
        // 现在10行
        assertEquals(10, all.size());
        LOGGER.info("2020-01-01存在的数据：");
        print(all);

        Calendar cal2 = Calendar.getInstance();
        cal2.set(Calendar.YEAR, 2020);
        cal2.set(Calendar.MONTH, 0);
        cal2.set(Calendar.DAY_OF_MONTH, 2);
        List<Map<String, Object>> created1 = dao.findByCreatedDate(cal.getTime(), cal2.getTime());
        assertEquals(11, created1.size());
        LOGGER.info(("2020-01-01~02增加的数据："));
        print(created1);

        List<Map<String, Object>> allHistory = dao.findAllHistory(1L);
        LOGGER.info(("1号记录的变化历史："));
        print(allHistory);

        allHistory = dao.findCreatedHistory(1L);
        LOGGER.info(("1号记录的增加历史："));
        print(allHistory);

        allHistory = dao.findUpdatedHistory(1L);
        LOGGER.info(("1号记录的更新历史："));
        print(allHistory);

        allHistory = dao.findDeletedHistory(1L);
        LOGGER.info(("1号记录的删除历史："));
        print(allHistory);

        List<Map<String, Object>> createdRecords = dao.diffCreated();
        LOGGER.info(("比较得到增加的记录："));
        print(createdRecords);
        for (Map<String, Object> record: createdRecords) {
            dao.create((Long)record.get("business_id"), (String)record.get("name"), (Date) record.get("sync_date"));
        }
        List<Map<String, Object>> deletedRecords = dao.diffDeleted();
        LOGGER.info(("比较得到删除的记录："));
        print(deletedRecords);
        for (Map<String, Object> record: deletedRecords) {
            dao.delete((Long)record.get("business_id"), (String)record.get("name"), new Date());
        }
        LOGGER.info(("同步之后的结果："));
        print(dao.findAllValid());
    }

    private void print(List<Map<String, Object>> data) {
        for (Map<String, Object> row : data) {
            LOGGER.info("" + row);
        }
    }
}
