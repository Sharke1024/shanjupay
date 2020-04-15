package com.shanjupay.transaction;

import com.shanjupay.transaction.api.PayChannelService;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @Author DL_Wu
 * @Date 2020/4/14 20:28
 * @Version 1.0
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestPayChannelService {

    @Autowired
    private PayChannelService payChannelService;

    @Test
    public void testQueryPayChannelByPlatformChannel(){
        List<PayChannelDTO> shanju_b2c = payChannelService.queryPayChannelByPlatformChannel("shanju_b2c");
        System.out.println(shanju_b2c);
    }

}
