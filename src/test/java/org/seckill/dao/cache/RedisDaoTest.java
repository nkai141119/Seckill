package org.seckill.dao.cache;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dao.SeckillDao;
import org.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;




@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring的配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class RedisDaoTest {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private long id = 1000;
	@Autowired
	private RedisDao redisDao;
	@Autowired
	private SeckillDao seckillDao;
	

	@Test
	public void testGetSeckill() {
		fail("Not yet implemented");
	}

	@Test
	public void testPutSeckill() {
		fail("Not yet implemented");
	}
	@Test
	public void testSeckill(){
		
		Seckill seckill = redisDao.getSeckill(id);
        if (seckill == null) {
            seckill = seckillDao.queryById(id);
            if (seckill != null) {
                String result = redisDao.putSeckill(seckill);
                logger.info("result={}", result);
                seckill = redisDao.getSeckill(id);
                logger.info("seckill={}", seckill);
            }
        }
        logger.info("seckill={}", seckill);

	}

}
