package org.seckill.dao.cache;

import org.seckill.entity.Seckill;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
//用于将seckill对象在redis进行存入、查询，由于redis无法直接存储对象，所以需要进行序列化。
//这里由于java自带的序列化Serializable性能不高，使用开源工具Protostuff进行序列化
//注意：使用protostuff序列化时，对象必须是pojo(具备get set方法)
public class RedisDao {
    private final JedisPool jedisPool;

    public RedisDao(String ip, int port) {
        jedisPool = new JedisPool(ip, port);
    }
    
    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);
    //从redis中试图查找该seckill对象是否存在
    public Seckill getSeckill(long seckillId) {
        // redis操作逻辑
        try {
            Jedis jedis = jedisPool.getResource();
            try {
                String key = "seckill:" + seckillId;
                // 并没有实现哪部序列化操作
                // 采用自定义序列化
                // protostuff: pojo.
                byte[] bytes = jedis.get(key.getBytes());
                // 缓存重获取到
                if (bytes != null) {
                    Seckill seckill = schema.newMessage();
                    ProtostuffIOUtil.mergeFrom(bytes, seckill, schema);
                    // seckill被反序列化

                    return seckill;
                }
            } finally {
                jedis.close();
            }
        } catch (Exception e) {

        }
        return null;
    }
    //已经从mysql中查询到seckill对象的基础上，将其存入redis中；
    public String putSeckill(Seckill seckill) {
        try {
            Jedis jedis = jedisPool.getResource();
            try {
                String key = "seckill:" + seckill.getSeckillId();
                byte[] bytes = ProtostuffIOUtil.toByteArray(seckill, schema,
                        LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                // 超时缓存
                int timeout = 60 * 60;// 1小时
                //Redis Setex 命令为指定的 key 设置值及其过期时间。如果 key 已经存在， SETEX 命令将会替换旧的值。
                String result = jedis.setex(key.getBytes(), timeout, bytes);

                return result;
            } finally {
                jedis.close();
            }
        } catch (Exception e) {

        }

        return null;
    }
}
