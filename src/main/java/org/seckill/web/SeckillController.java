package org.seckill.web;

import java.util.Date;
import java.util.List;

import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.dto.SeckillResult;
import org.seckill.entity.Seckill;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by codingBoy on 16/11/28.
 */
@Component
@RequestMapping("/seckill")//使用restful风格，url:模块/资源/{}/细分 
public class SeckillController
{	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SeckillService seckillService;

    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public String list(Model model)
    {
        //list.jsp+mode=ModelAndView
        //获取列表页
        List<Seckill> list=seckillService.getSeckillList();
        model.addAttribute("list",list);
        return "list";//由于配置，其实返回的是/WEB-INF/jsp/"list".jsp
    }

    @RequestMapping(value = "/{seckillId}/detail",method = RequestMethod.GET)
    public String detail(@PathVariable("seckillId") Long seckillId, Model model)
    {	
    	//获取详情页
        if (seckillId == null)
        {
            return "redirect:/seckill/list";//重定向到对list的请求，回到列表页。浏览器会再发出一次请求，地址栏会变化
        }

        Seckill seckill=seckillService.getById(seckillId);
        if (seckill==null)
        {
            return "forward:/seckill/list";//转发，浏览器不会重新请求，而是内部服务的转发，地址栏不变。效率会比redirect高。
            //多用于
        }

        model.addAttribute("seckill",seckill);

        return "detail";//其实是WEB-INF/jsp/seckill/detail.jsp
    }

    //ajax ,json暴露秒杀接口的方法
    @RequestMapping(value = "/{seckillId}/exposer",
                    method = RequestMethod.POST,
                    produces = {"application/json;charset=UTF-8"})//指定content-type，使返回的json能够以正确的格式被读取
    @ResponseBody//这个注解会使springmvc将seckilResult封装成json
    //这里的@pathVatiable千万不能忘了写否则seckillId就无法自动获取！！！然后就会NullpointerEsception。。。排查了好久才找到
    public SeckillResult<Exposer> exposer(@PathVariable("seckillId") Long seckillId)
    {
        SeckillResult<Exposer> result;
        try{
        	//logger.info("seckillId="+seckillId);
            Exposer exposer=seckillService.exportSeckillUrl(seckillId);
            //logger.info("Exposer exposer=seckillService.exportSeckillUrl(seckillId);");
            result=new SeckillResult<Exposer>(true,exposer);
            logger.info("exposer success!!");
        }catch (Exception e)
        {
            e.printStackTrace();
            //logger.info("e.printStackTrace();");
            //logger.info("result=new SeckillResult<Exposer>(false,e.getMessage());");
            result=new SeckillResult<Exposer>(false,e.getMessage());
        }
        //仅仅是说明exposer成功返回，但是不一定成功获取到了地址（md5）,详见exportSeckilUrl
        //logger.info("return result;");
        return result;
    }

    @RequestMapping(value = "/{seckillId}/{md5}/execution",
            method = RequestMethod.POST,//非幂等，也就是两次可能会造成两个结果
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<SeckillExecution> execute(@PathVariable("seckillId") Long seckillId,
                                                   @PathVariable("md5") String md5,
                                                   //这里原来写的是killPhone但是存储在浏览器的cookie里是userPhone所以必须保持统一，否则为null
                                                   @CookieValue(value = "userPhone",required = false) Long phone)//phone是从用户的浏览器的cookie中获取
    //这里required=false，即使浏览器的phone为空，也不会报错，而是在以下的程序中处理
    //也可以采用springmvc valid验证，但是这里参数比较简单，所以就用直接的方式
    {	
    	logger.info("execute begin");
        if (phone==null)
        {
        	logger.info("phone == null");
            return new SeckillResult<SeckillExecution>(false,"未注册");
        }
        
        SeckillResult<SeckillExecution> result;

        try {
        	logger.info("executeSeckill");
            SeckillExecution execution = seckillService.executeSeckill(seckillId, phone, md5);
            return new SeckillResult<SeckillExecution>(true, execution);
        }catch (RepeatKillException e1)
        {
        	logger.info("repeatkill");
            SeckillExecution execution=new SeckillExecution(seckillId, SeckillStatEnum.REPEAT_KILL);
            return new SeckillResult<SeckillExecution>(true,execution);
            //请求成功，但是是重复秒杀
        }catch (SeckillCloseException e2)
        {
        	logger.info("seckillclose");
            SeckillExecution execution=new SeckillExecution(seckillId, SeckillStatEnum.END);
            return new SeckillResult<SeckillExecution>(true,execution);
            //请求成功，但是秒杀已关闭
        }
        catch (Exception e)
        {
        	logger.info("other exception");
            SeckillExecution execution=new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);
            return new SeckillResult<SeckillExecution>(true,execution);
            //内部错误，但是也返回true
        }

    }

    //获取系统时间
    @RequestMapping(value = "/time/now",method = RequestMethod.GET)
    @ResponseBody
    public SeckillResult<Long> time()
    {
        Date now=new Date();
        return new SeckillResult<Long>(true,now.getTime());
    }
}























