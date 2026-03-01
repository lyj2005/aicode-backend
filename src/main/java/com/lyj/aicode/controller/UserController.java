package com.lyj.aicode.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyj.aicode.annotation.AuthCheck;
import com.lyj.aicode.common.BaseResponse;
import com.lyj.aicode.common.DeleteRequest;
import com.lyj.aicode.common.ErrorCode;
import com.lyj.aicode.common.ResultUtils;
import com.lyj.aicode.constant.UserConstant;
import com.lyj.aicode.exception.BusinessException;
import com.lyj.aicode.exception.ThrowUtils;
import com.lyj.aicode.model.dto.*;
import com.lyj.aicode.model.entity.User;
import com.lyj.aicode.model.vo.LoginUserVO;
import com.lyj.aicode.model.vo.UserVO;
import com.lyj.aicode.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.lyj.aicode.service.impl.UserServiceImpl.SALT;

@RestController
@RequestMapping("/user")
public class UserController {



    @Resource
    private UserService userService;

    //region登录注册代码

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        //1. 校验
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        //2. 获取参数
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        //3. 注册
        long result = userService.userRegister(userAccount,userPassword,checkPassword);
        //4. 返回结果
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        //1. 校验
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        //2. 获取参数
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        //3. 登录
        LoginUserVO loginUserVO = userService.userLogin(userAccount,userPassword, request);
        //4. 返回结果
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/current")
    public BaseResponse<LoginUserVO> currentUser(HttpServletRequest request) {
        //1. 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        //2. 返回结果
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    /**
     * 用户注销
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        //1. 校验
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        //2. 注销
        boolean result = userService.userLogout(request);
        //3. 返回结果
        return ResultUtils.success(result);
    }

    //endregion


    // region 增删改查代码

    /**
     * 创建用户
     *
     * @param userAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        //1. 校验输入
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2. 创建用户
        User user = new User();
        //3. 复制属性
        BeanUtils.copyProperties(userAddRequest, user);
        //4. 设置默认密码 12345678
        String defaultPassword = "12345678";
        //5. 密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + defaultPassword).getBytes());
        //6. 设置用户密码
        user.setUserPassword(encryptPassword);
        //7. 保存用户到数据库
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        //返回结果
        return ResultUtils.success(user.getId());
    }

    /**
     * 删除用户
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        //1. 参数校验
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2. 删除
        boolean b = userService.removeById(deleteRequest.getId());
        //3. 返回结果
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     *
     * @param userUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
                                            HttpServletRequest request) {
        //1. 参数校验
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2. 创建新用户
        User user = new User();
        //3. 更新
        BeanUtils.copyProperties(userUpdateRequest, user);
        //4. 写入数据库
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);//失败返回信息
        //5. 返回结果
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户（仅管理员）
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id, HttpServletRequest request) {
        //1. 校验参数
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2. 获取用户
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        //3. 返回结果
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id, HttpServletRequest request) {
        //1. 获取用户
        BaseResponse<User> response = getUserById(id, request);
        //2. 获取用户数据
        User user = response.getData();
        //3. 转换得到结果
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 分页获取用户列表（仅管理员）
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest,
                                                   HttpServletRequest request) {
        //1. 获取当前页和大小
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        //2. 获取分页数据
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        //3. 返回
        return ResultUtils.success(userPage);
    }

    /**
     * 分页获取用户封装列表
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest,
                                                       HttpServletRequest request) {
        //1. 参数校验
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2. 获取当前页和大小
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        //3. 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        //4. 获取分页数据
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        //5. 封装数据
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        //6. 返回结果
        return ResultUtils.success(userVOPage);
    }

    // endregion




    /**
     * 更新个人信息
     *
     * @param userUpdateMyRequest
     * @param request
     * @return
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest,
                                              HttpServletRequest request) {
        //1. 校验
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2. 获取当前用户
        User loginUser = userService.getLoginUser(request);
        //3. 更新
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());
        //4. 写入到数据库中
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        //5. 返回结果
        return ResultUtils.success(true);
    }
}


