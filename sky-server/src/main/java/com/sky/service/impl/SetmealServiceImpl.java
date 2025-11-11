package com.sky.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Wrapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增套餐
     * @param setmealDTO
     * @return
     */
    @Override
    public void saveWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmeal.setCreateTime(LocalDateTime.now());
        setmeal.setUpdateTime(LocalDateTime.now());
        setmeal.setCreateUser(BaseContext.getCurrentId());
        setmeal.setUpdateUser(BaseContext.getCurrentId());
        setmealMapper.insert(setmeal);

        Long id = setmeal.getId();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(id);
        }
        setmealDishMapper.insert(setmealDishes);
    }

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        int pageNum = setmealPageQueryDTO.getPage();
        int pageSize = setmealPageQueryDTO.getPageSize();

        PageHelper.startPage(pageNum, pageSize);
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除套餐
     * @param ids
     * @return
     */
    @Override
    public void deleteBatch(List<Long> ids) {
        for (Long id : ids)
        {
            Setmeal setmeal = setmealMapper.selectById(id);
            if (setmeal.getStatus() == StatusConstant.ENABLE)
            {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        setmealMapper.deleteByIds(ids);
        for (Long id : ids) {
            setmealDishMapper.delete(
                    Wrappers.<SetmealDish>lambdaQuery()
                            .eq(SetmealDish::getSetmealId, id)
            );
        }
    }

    /**
     * 修改套餐
     *
     * @param setmealDTO
     * @return
     */
    @Transactional
    @Override
    public void updateBySetmealDTO(SetmealDTO setmealDTO) {
        setmealMapper.update(
                Wrappers.<Setmeal>lambdaUpdate()
                        .eq(Setmeal::getId, setmealDTO.getId())
                        .set(Setmeal::getUpdateTime, LocalDateTime.now())
                        .set(Setmeal::getUpdateUser,BaseContext.getCurrentId())
                        .set(setmealDTO.getStatus() != null,Setmeal::getStatus,setmealDTO.getStatus())
                        .set(setmealDTO.getPrice() != null,Setmeal::getPrice,setmealDTO.getPrice())
                        .set(StrUtil.isNotBlank(setmealDTO.getDescription()),Setmeal::getDescription,setmealDTO.getDescription())
                        .set(StrUtil.isNotBlank(setmealDTO.getImage()),Setmeal::getImage,setmealDTO.getImage())
                        .set(StrUtil.isNotBlank(setmealDTO.getName()),Setmeal::getName,setmealDTO.getName())

        );

        Long id = setmealDTO.getId();
        setmealDishMapper.delete(
                Wrappers.<SetmealDish>lambdaQuery()
                        .eq(SetmealDish::getSetmealId, id)
        );


        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(id);
        }
        setmealDishMapper.insert(setmealDishes);
    }
}
