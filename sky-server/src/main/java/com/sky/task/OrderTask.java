package com.sky.task;


import com.sky.entity.Orders;
import com.sky.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderService orderService;


    /**
     * 定时处理超时订单
     */
    @Scheduled(cron = "0 * * * * *")
    //@Scheduled(cron = "1/5  * * * * ?")
    public void processTimeOutOrder() {
        log.info("定时处理超时订单:{}", LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);

        List<Orders> ordersList = orderService.lambdaQuery()
                .eq(Orders::getStatus,Orders.PENDING_PAYMENT)
                .le(Orders::getOrderTime,time)
                .list();

        if (ordersList != null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时,自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderService.updateById(orders);
            }
        }


    }

    /**
     * 定时处理一直在派送的订单
     */
    @Scheduled(cron = "0 0 1 * * ?")
    //@Scheduled(cron = "0/5  * * * * ?")
    public void processDeliverOrder() {
    log.info("定时处理一直在派送的订单:{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusHours(1L);

        List<Orders> ordersList = orderService.lambdaQuery()
                .eq(Orders::getStatus,Orders.DELIVERY_IN_PROGRESS)
                .le(Orders::getOrderTime,time)
                .list();

        if (ordersList != null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orderService.updateById(orders);
            }
        }

    }

}
