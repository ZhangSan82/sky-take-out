package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import org.springframework.stereotype.Service;

import java.util.List;


public interface SetmealService extends IService<Setmeal> {

    /**
     * 新增套餐
     * @param setmealDTO
     * @return
     */
    void saveWithDish(SetmealDTO setmealDTO);

    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 批量删除套餐
     * @param ids
     * @return
     */
    void deleteBatch(List<Long> ids);

    /**
     * 修改套餐
     *
     * @param setmealDTO
     * @return
     */
    void updateBySetmealDTO(SetmealDTO setmealDTO);
}
