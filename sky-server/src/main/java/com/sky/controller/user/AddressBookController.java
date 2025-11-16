package com.sky.controller.user;

import com.github.xiaoymin.knife4j.core.util.StrUtil;
import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/user/addressBook")
@Api(tags = "C端地址簿接口")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 查询当前登录用户的所有地址信息
     *
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("查询当前登录用户的所有地址信息")
    public Result<List<AddressBook>> list() {
        //AddressBook addressBook = new AddressBook();
        //addressBook.setUserId(BaseContext.getCurrentId());
        List<AddressBook> list = addressBookService.lambdaQuery()
                .eq(AddressBook::getUserId,4l)
                .list();
        return Result.success(list);
    }

    /**
     * 新增地址
     *
     * @param addressBook
     * @return
     */
    @PostMapping
    @ApiOperation("新增地址")
    public Result save(@RequestBody AddressBook addressBook) {
        addressBook.setIsDefault(0);
        addressBook.setUserId(4L);
        addressBookService.save(addressBook);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询地址")
    public Result<AddressBook> getById(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.getById(id);
        return Result.success(addressBook);
    }

    /**
     * 根据id修改地址
     *
     * @param addressBook
     * @return
     */
    @PutMapping
    @ApiOperation("根据id修改地址")
    public Result update(@RequestBody AddressBook addressBook) {
        addressBookService.lambdaUpdate()
                .eq(AddressBook::getUserId,addressBook.getUserId())
                .set(StrUtil.isNotBlank(addressBook.getDetail()),AddressBook::getDetail,addressBook.getDetail())
                .set(StrUtil.isNotBlank(addressBook.getConsignee()),AddressBook::getConsignee,addressBook.getConsignee())
                .set(StrUtil.isNotBlank(addressBook.getCityCode()),AddressBook::getCityCode,addressBook.getCityCode())
                .set(StrUtil.isNotBlank(addressBook.getDistrictName()),AddressBook::getDistrictName,addressBook.getDistrictName())
                .set(StrUtil.isNotBlank(addressBook.getDistrictCode()),AddressBook::getDistrictCode,addressBook.getDistrictCode())
                .set(StrUtil.isNotBlank(addressBook.getCityName()),AddressBook::getCityName,addressBook.getCityName())
                .set(StrUtil.isNotBlank(addressBook.getPhone()),AddressBook::getPhone,addressBook.getPhone())
                .set(StrUtil.isNotBlank(addressBook.getLabel()),AddressBook::getLabel,addressBook.getLabel())
                .set(addressBook.getIsDefault()!= null,AddressBook::getIsDefault,addressBook.getIsDefault())
                .set(StrUtil.isNotBlank(addressBook.getProvinceCode()),AddressBook::getProvinceCode,addressBook.getProvinceCode())
                .set(StrUtil.isNotBlank(addressBook.getProvinceName()),AddressBook::getProvinceName,addressBook.getProvinceName())
                .set(StrUtil.isNotBlank(addressBook.getSex()),AddressBook::getSex,addressBook.getSex())
                .update();

        return Result.success();
    }

    /**
     * 设置默认地址
     *
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    @ApiOperation("设置默认地址")
    public Result setDefault(@RequestBody AddressBook addressBook) {
        addressBookService.lambdaUpdate()
                .set(AddressBook::getIsDefault,0)
                .update();
        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);
        return Result.success();
    }

    /**
     * 根据id删除地址
     *
     * @param id
     * @return
     */
    @DeleteMapping
    @ApiOperation("根据id删除地址")
    public Result deleteById(Long id) {
        addressBookService.removeById(id);
        return Result.success();
    }

    /**
     * 查询默认地址
     */
    @GetMapping("default")
    @ApiOperation("查询默认地址")
    public Result<AddressBook> getDefault() {
        //SQL:select * from address_book where user_id = ? and is_default = 1
//        AddressBook addressBook = new AddressBook();
//        addressBook.setIsDefault(1);
//        addressBook.setUserId(BaseContext.getCurrentId());
        List<AddressBook> list = addressBookService.lambdaQuery()
                .eq(AddressBook::getIsDefault,1)
                .list();

        if (list != null && list.size() == 1) {
            return Result.success(list.get(0));
        }

        return Result.error("没有查询到默认地址");
    }

}
