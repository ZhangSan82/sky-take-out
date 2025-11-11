package com.sky.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    /**
     * 新增菜品及口味
     * @param dishDTO
     * @return
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {

        //向菜品插入一条数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dish.setCreateTime(LocalDateTime.now());
        dish.setUpdateTime(LocalDateTime.now());
        dish.setCreateUser(BaseContext.getCurrentId());
        dish.setUpdateUser(BaseContext.getCurrentId());
        dishMapper.insert(dish);
        Long id = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            //向口味表插入数据
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(id);
            }
           dishFlavorMapper.insert(flavors);
        }

    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 菜品的批量删除
     * @param ids
     * @return
     */
    @Override
    public void deleteByIds(List<Long> ids) {
        //判断菜品状态
        for (Long id : ids) {
            Dish dish = dishMapper.selectById(id);
            if(dish.getStatus() == StatusConstant.ENABLE)
            {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //判断是否关联套餐
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        //删除菜品
        dishMapper.deleteByIds(ids);

        //删除菜品关联口味
        // 根据 dish_id 批量删除
        // 根据菜品 id 集合批量删除口味
        dishFlavorMapper.delete(
                Wrappers.<DishFlavor>lambdaQuery()
                        .in(DishFlavor::getDishId, ids)
        );
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {

       DishVO dishVO = new DishVO();
       Dish dish = dishMapper.selectById(id);
       BeanUtils.copyProperties(dish,dishVO);

       List<DishFlavor> dishFlavors = dishFlavorMapper.selectList(
               Wrappers.<DishFlavor>lambdaQuery()
               .eq(DishFlavor::getDishId, id)
       );
       dishVO.setFlavors(dishFlavors);
       return dishVO;
    }

    /**
     * 修改菜品
     * @param dishDTO
     * @return
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        dishMapper.update(
                Wrappers.<Dish>lambdaUpdate()
                .eq(Dish::getId, dishDTO.getId())
                        .set(StrUtil.isNotBlank(dishDTO.getName()),Dish::getName, dishDTO.getName())
                        .set(dishDTO.getPrice() != null,Dish::getPrice, dishDTO.getPrice())
                        .set(Dish::getUpdateTime, LocalDateTime.now())
                        .set(Dish::getUpdateUser, BaseContext.getCurrentId())
                        .set(dishDTO.getStatus() != null,Dish::getStatus, dishDTO.getStatus())
                        .set(StrUtil.isNotBlank(dishDTO.getDescription()),Dish::getDescription, dishDTO.getDescription())
                        .set(StrUtil.isNotBlank(dishDTO.getImage()),Dish::getImage,dishDTO.getImage())
        );
        dishFlavorMapper.delete(
                Wrappers.<DishFlavor>lambdaQuery()
                .eq(DishFlavor::getDishId, dishDTO.getId())
        );

        List<DishFlavor> flavors = dishDTO.getFlavors();
        Long id = dishDTO.getId();
        if (flavors != null && flavors.size() > 0) {
            //向口味表插入数据
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(id);
            }
            dishFlavorMapper.insert(flavors);
        }
    }
}
