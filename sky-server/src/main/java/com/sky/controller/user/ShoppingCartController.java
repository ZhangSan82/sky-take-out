package com.sky.controller.user;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.service.SetmealService;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Api(tags = "C端购物车接口")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private DishService dishService;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     * @return
     */
    @PostMapping("/add")
    @ApiOperation("添加购物车")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("添加购物车:{}",shoppingCartDTO);

        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        Long userId = BaseContext.getCurrentId();
        log.info("userId:{}",userId);
        shoppingCart.setUserId(4L);

        //判断当前添加的商品是否已经存在
        List<ShoppingCart> list = shoppingCartService.lambdaQuery()
                .eq(BaseContext.getCurrentId() != null,ShoppingCart::getUserId, BaseContext.getCurrentId())
                .eq(shoppingCartDTO.getSetmealId() != null,ShoppingCart::getSetmealId,shoppingCartDTO.getSetmealId())
                .eq(shoppingCartDTO.getDishId() != null,ShoppingCart::getDishId,shoppingCartDTO.getDishId())
                .eq(shoppingCartDTO.getDishFlavor() != null,ShoppingCart::getDishFlavor,shoppingCartDTO.getDishFlavor())
                .list();
        //存在则加一
        if (list != null && list.size() > 0){
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber()+1);

            shoppingCartService.lambdaUpdate()
                    .eq(ShoppingCart::getId,cart.getId())
                    .set(ShoppingCart::getNumber,cart.getNumber())
                    .update();
        }
        else{
                //判断是菜品还是套餐
                Long dishid = shoppingCartDTO.getDishId();
                if(dishid != null){
                    Dish dish = dishService.getById(dishid);
                    shoppingCart.setName(dish.getName());
                    shoppingCart.setImage(dish.getImage());
                    shoppingCart.setAmount(dish.getPrice());

                }else {
                    Long setmealId = shoppingCartDTO.getSetmealId();
                    Setmeal setmeal = setmealService.getById(setmealId);
                    shoppingCart.setName(setmeal.getName());
                    shoppingCart.setImage(setmeal.getImage());
                    shoppingCart.setAmount(setmeal.getPrice());
                }
                shoppingCart.setNumber(1);
                shoppingCart.setCreateTime(LocalDateTime.now());
                shoppingCartService.save(shoppingCart);
        }
        return Result.success();
    }

    /**
     *查看购物车
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("查看购物车")
    public Result<List<ShoppingCart>> list(){
        List<ShoppingCart> list = shoppingCartService.lambdaQuery().list();
        return Result.success(list);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    @ApiOperation("清空购物车")
    public Result clean(){

        shoppingCartService.lambdaUpdate()
                .eq(ShoppingCart::getUserId,4L)
                .remove();
        return Result.success();
    }

    /**
     * 删除购物车中一个商品
     * @param shoppingCartDTO
     * @return
     */
    @PostMapping("/sub")
    @ApiOperation("删除购物车中一个商品")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("删除购物车中一个商品，商品：{}", shoppingCartDTO);
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        List<ShoppingCart> list = shoppingCartService.lambdaQuery()
                .eq(ShoppingCart::getUserId,4L)
                .list();

        if (list != null && list.size() > 0){
            shoppingCart = list.get(0);
            Integer number = shoppingCart.getNumber();
            if (number == 1){
                shoppingCartService.removeById(shoppingCart.getId());
            }
            else {
                shoppingCart.setNumber(number-1);
                shoppingCartService.updateById(shoppingCart);
            }
        }
        return Result.success();
    }
}


