package com.sky.controller.admin;


import ch.qos.logback.core.joran.util.beans.BeanUtil;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/category")
@Api(tags = "分类相关接口")
@Slf4j

public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /*
    新增分类
     */
    @PostMapping
    @ApiOperation("新增分类")
    public Result<String> save(@RequestBody CategoryDTO categoryDTO){
        log.info("新增分类：{}", categoryDTO);
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO,category);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        category.setCreateUser(BaseContext.getCurrentId());
        category.setUpdateUser(BaseContext.getCurrentId());
        category.setStatus(StatusConstant.DISABLE);
        categoryService.save(category);
        return Result.success();
    }

    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分类分页查询")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO){
        log.info("分页查询：{}", categoryPageQueryDTO);
        PageResult pageResult = categoryService.pageQuery(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 删除分类
     * @param id
     * @return
     */
    @DeleteMapping
    @ApiOperation("删除分类")
    public Result<String> deleteById(Long id) {
        log.info("删除分类：{}", id);
        categoryService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改分类
     * @param categoryDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改分类")
    public Result<String> update(@RequestBody CategoryDTO categoryDTO){
        log.info("根据id:{},修改分类", categoryDTO.getId());
        Category category = new Category();
         categoryService.lambdaUpdate()
                 .eq(Category::getId, categoryDTO.getId())
                 .set(Category::getName, categoryDTO.getName())
                 .set(Category::getSort, categoryDTO.getSort())
                 .set(Category::getType, categoryDTO.getType())
                 .set(Category::getUpdateTime, LocalDateTime.now())
                 .set(Category::getUpdateUser, BaseContext.getCurrentId())
                 .update();
        return Result.success();
    }


    /**
     * 启用、禁用分类
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用分类")
    public Result<String> startOrStop(@PathVariable("status") Integer status, Long id){
        log.info("根据id:{},控制状态",id);
        categoryService.lambdaUpdate()
                .eq(Category::getId,id)
                .set(Category::getStatus, status)
                .set(Category::getUpdateTime, LocalDateTime.now())
                .set(Category::getUpdateUser, BaseContext.getCurrentId())
                .update();
        return Result.success();
    }

    /**
     * 根据类型查询分类
     * @param type
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据类型查询分类")
    public Result<List<Category>> list(Integer type){

        log.info("根据type:{},查询", type);
        return Result.success(categoryService.lambdaQuery().eq(Category::getType, type).list());
    }


}
