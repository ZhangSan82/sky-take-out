package com.sky.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
//        Dish dish = new Dish();
//        dish.setCategoryId(categoryId);
//        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品

        //List<DishVO> list = dishService.listWithFlavor(dish);

        //构造key
        String key = "dish_" +  categoryId;

        //查询rides是否存在
        List<DishVO> dishVOS = (List<DishVO>)redisTemplate.opsForValue().get(key);

        if (dishVOS != null && dishVOS.size() > 0) {
            return Result.success(dishVOS);
        }

        List<DishVO> list = new ArrayList<>();
        List<Dish> list2 = dishService.lambdaQuery()
                .eq(Dish::getCategoryId, categoryId)
                .eq(Dish::getStatus,StatusConstant.ENABLE)
                .list();

        for (Dish dish : list2) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(dish, dishVO);
            List<DishFlavor> dishFlavors = dishFlavorMapper.selectList(
                    Wrappers.<DishFlavor>lambdaQuery()
                            .eq(DishFlavor::getDishId, dish.getId())
            );

            dishVO.setFlavors(dishFlavors);
            list.add(dishVO);

        }
        redisTemplate.opsForValue().set(key, list);
        return Result.success(list);
    }

}
