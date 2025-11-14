package com.sky.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
//import springfox.documentation.annotations.Cacheable;
//import springfox.documentation.annotations.Cacheable;

import java.util.ArrayList;
import java.util.List;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Api(tags = "C端-套餐浏览接口")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 条件查询
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询套餐")
    @Cacheable(cacheNames = "setmealCache",key = "#categoryId")
    public Result<List<Setmeal>> list(Long categoryId) {
//        Setmeal setmeal = new Setmeal();
//        setmeal.setCategoryId(categoryId);
//        setmeal.setStatus(StatusConstant.ENABLE);
//
//        List<Setmeal> list = setmealService.list(setmeal);
        List<Setmeal> list = setmealService.lambdaQuery()
                .eq(Setmeal::getCategoryId, categoryId)
                .eq(Setmeal::getStatus, StatusConstant.ENABLE)
                .list();
        return Result.success(list);
    }

    /**
     * 根据套餐id查询包含的菜品列表
     *
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    @ApiOperation("根据套餐id查询包含的菜品列表")
    public Result<List<DishItemVO>> dishList(@PathVariable("id") Long id) {

        List<DishItemVO> list = new ArrayList<>();
        List<SetmealDish> list1 = setmealDishMapper.selectList(
                new QueryWrapper<SetmealDish>().lambda()
                .eq(SetmealDish::getSetmealId, id)
        );
        for (SetmealDish setmealDish : list1) {
            DishItemVO dishItemVO = new DishItemVO();
            dishItemVO.setName(setmealDish.getName());
            dishItemVO.setCopies(setmealDish.getCopies());

            Dish dish = dishMapper.selectById(setmealDish.getDishId());
            dishItemVO.setDescription(dish.getDescription());
            dishItemVO.setImage(dish.getImage());

            list.add(dishItemVO);
        }


        //List<DishItemVO> list = setmealService.getDishItemById(id);
        return Result.success(list);
    }
}
