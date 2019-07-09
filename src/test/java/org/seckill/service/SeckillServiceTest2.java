package org.seckill.service;

import java.util.List;

//import org.jboss.logging.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
		{"classpath:spring/spring-dao.xml","classpath:spring/spring-service.xml"})
public class SeckillServiceTest2 {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private SeckillService seckillService;
	
	@Test
	public void testGetSeckillList() {
		List<Seckill> list = seckillService.getSeckillList();
		logger.info("list={}",list);//info级别，由于当前配置logback处于debug级别（低于info）所以肯定会输出
	}

	@Test
	public void testGetById() {
		long id = 1000;
		Seckill seckill = seckillService.getById(id);
		logger.info("seckill= {}",seckill);
	}

	@Test
	public void testExportSeckillUrl() {
		long id = 1000;
		Exposer exposer = seckillService.exportSeckillUrl(id);
		logger.info("exposer = {}",exposer);
		//exposer = Exposer{exposed=true, md5='bf204e2683e7452aa7db1a50b5713bae', seckillId=1000, now=0, start=0, end=0}
		//修改了表数据，让id=1000的商品秒杀时间持续长达一个月，也就是使现在处于秒杀状态中，从而可以获得md5，并且时间都为空，因为不需要
	
	}

	@Test
	public void testExecuteSeckill() {
		long id  = 1000;
		long phone = 13200015253L;
		String md5 = "bf204e2683e7452aa7db1a50b5713bae";
		try {
			SeckillExecution seckillExecution = seckillService.executeSeckill(id, phone, md5);
			logger.info("seckillExecution = {}",seckillExecution);
		} catch (RepeatKillException e) {
			// TODO Auto-generated catch block
			logger.info(e.getMessage());
		} catch (SeckillCloseException e) {
			// TODO Auto-generated catch block
			logger.info(e.getMessage());
		}
		/*第一次提交成功，再次运行时，触发事务机制。DEBUG org.mybatis.spring.SqlSessionUtils - Registering transaction synchronization for SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@55c53a33]
14:45:57.477 [main] DEBUG o.m.s.t.SpringManagedTransaction - JDBC Connection [com.mchange.v2.c3p0.impl.NewProxyConnection@3ddc6915] will be managed by Spring
14:45:57.480 [main] DEBUG o.s.dao.SeckillDao.reduceNumber - ==>  Preparing: UPDATE seckill SET number = number-1 WHERE seckill_id=? AND start_time <= ? AND end_time >= ? AND number > 0; 
14:45:57.506 [main] DEBUG o.s.dao.SeckillDao.reduceNumber - ==> Parameters: 1000(Long), 2019-07-07 14:45:57.456(Timestamp), 2019-07-07 14:45:57.456(Timestamp)
14:45:57.508 [main] DEBUG o.s.dao.SeckillDao.reduceNumber - <==    Updates: 1 减库存成功
14:45:57.508 [main] DEBUG org.mybatis.spring.SqlSessionUtils - Releasing transactional SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@55c53a33]
14:45:57.509 [main] DEBUG org.mybatis.spring.SqlSessionUtils - Fetched SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@55c53a33] from current transaction
14:45:57.509 [main] DEBUG o.s.d.S.insertSuccessKilled - ==>  Preparing: INSERT ignore INTO success_killed(seckill_id,user_phone,state) VALUES (?,?,0) 
14:45:57.510 [main] DEBUG o.s.d.S.insertSuccessKilled - ==> Parameters: 1000(Long), 13200015253(Long)
14:45:57.510 [main] DEBUG o.s.d.S.insertSuccessKilled - <==    Updates: 0 由于重复写入，所以不允许，insertCount<=0 ，抛出运行时异常，引发事务
14:45:57.517 [main] DEBUG org.mybatis.spring.SqlSessionUtils - Releasing transactional SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@55c53a33]
14:45:57.517 [main] DEBUG org.mybatis.spring.SqlSessionUtils - Transaction synchronization deregistering SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@55c53a33]
14:45:57.517 [main] DEBUG org.mybatis.spring.SqlSessionUtils - Transaction synchronization closing SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@55c53a33]
七月 07, 2019 2:45:57 下午 org.springframework.context.support.GenericApplicationContext doClose
信息: Closing org.springframework.context.support.GenericApplicationContext@3e57cd70: startup date [Sun Jul 07 14:45:56 CST 2019]; root of context hierarchy

		 * */
		
	}

}
