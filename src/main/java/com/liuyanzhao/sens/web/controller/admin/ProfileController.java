package com.liuyanzhao.sens.web.controller.admin;

import com.liuyanzhao.sens.entity.Role;
import com.liuyanzhao.sens.entity.ThirdAppBind;
import com.liuyanzhao.sens.entity.User;
import com.liuyanzhao.sens.model.dto.JsonResult;
import com.liuyanzhao.sens.model.enums.ResultCodeEnum;
import com.liuyanzhao.sens.service.AuthService;
import com.liuyanzhao.sens.service.RoleService;
import com.liuyanzhao.sens.service.ThirdAppBindService;
import com.liuyanzhao.sens.service.UserService;
import com.liuyanzhao.sens.utils.LocaleMessageUtil;
import com.liuyanzhao.sens.utils.Md5Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

/**
 * <pre>
 *     后台用户管理控制器
 * </pre>
 *
 * @author : saysky
 * @date : 2017/12/24
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/user")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private ThirdAppBindService thirdAppBindService;


    @Autowired
    private LocaleMessageUtil localeMessageUtil;

    /**
     * 获取用户信息并跳转
     *
     * @return 模板路径admin/admin_profile
     */
    @GetMapping("/profile")
    @RequiresPermissions("user:profile")
    public String profile(Model model) {
        Subject subject = SecurityUtils.getSubject();
        //1.用户信息
        User user = (User) subject.getPrincipal();
        model.addAttribute("user", user);

        //2.第三方信息
        List<ThirdAppBind> thirdAppBinds = thirdAppBindService.findByUserId(user.getUserId());
        model.addAttribute("thirdAppBinds", thirdAppBinds);

        //3.角色列表
        List<Role> roles = roleService.listRolesByUserId(user.getUserId());
        model.addAttribute("roles", roles);
        return "admin/admin_profile";
    }


    /**
     * 处理修改用户资料的请求
     *
     * @param user user
     * @return JsonResult
     */
    @PostMapping(value = "/profile/save")
    @ResponseBody
    @RequiresPermissions("user:profile-update")
    public JsonResult saveProfile(@Valid @ModelAttribute User user,
                                  BindingResult result) {
        try {
            if (result.hasErrors()) {
                for (ObjectError error : result.getAllErrors()) {
                    return new JsonResult(ResultCodeEnum.FAIL.getCode(), error.getDefaultMessage());
                }
            }
            Subject subject = SecurityUtils.getSubject();
            User principal = (User) subject.getPrincipal();
            //1.添加/更新用户名检查用户名是否存在
            User nameCheck = userService.findByUserName(user.getUserName());
            if (nameCheck != null && !Objects.equals(nameCheck.getUserId(), principal.getUserId())) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.user.user-name-exist"));
            }
            //2.添加/更新用户名检查用户名是否存在
            User emailCheck = userService.findByEmail(user.getUserEmail());
            if (emailCheck != null && !Objects.equals(emailCheck.getUserId(), principal.getUserId())) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.user.user-email-exist"));
            }

            user.setUserId(principal.getUserId());
            userService.saveByUser(user);
        } catch (Exception e) {
            log.error("修改用户资料失败：{}", e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.edit-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.edit-success-login-again"));
    }


    /**
     * 处理修改密码的请求
     *
     * @param beforePass 旧密码
     * @param newPass    新密码
     * @return JsonResult
     */
    @PostMapping(value = "/changePass")
    @ResponseBody
    @RequiresPermissions("user:password-update")
    public JsonResult changePass(@ModelAttribute("beforePass") String beforePass,
                                 @ModelAttribute("newPass") String newPass) {
        try {
            Subject subject = SecurityUtils.getSubject();
            User principal = (User) subject.getPrincipal();
            User user = userService.findByUserIdAndUserPass(principal.getUserId(), Md5Util.toMd5(beforePass, "sens", 10));
            if (null != user) {
                String password = Md5Util.toMd5(newPass, "sens", 10);
                userService.updatePassword(user.getUserId(), password);
            } else {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.user.old-password-error"));
            }
        } catch (Exception e) {
            log.error("修改密码失败：{}", e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.user.update-password-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.user.update-password-success"));
    }

    /**
     * 删除第三方关联
     *
     * @param bindId 关联
     * @return JsonResult
     */
    @PostMapping(value = "/removeBind")
    @ResponseBody
    @RequiresPermissions("user:bind-delete")
    public JsonResult checkDelete(@RequestParam("bindId") Long bindId) {
        try {
            //1.判断是否存在
            ThirdAppBind thirdAppBind = thirdAppBindService.findByThirdAppBindId(bindId);
            if (thirdAppBind == null) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.bind-not-exist"));
            }
            //2.判断是不是本人
            Subject subject = SecurityUtils.getSubject();
            User user = (User) subject.getPrincipal();
            if (!Objects.equals(thirdAppBind.getUserId(), user.getUserId())) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.permission-denied"));
            }
            //3.删除
            thirdAppBindService.removeById(bindId);
        } catch (Exception e) {
            log.error("删除关联失败, bindId:{}, cause:{}", bindId, e);
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.delete-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.delete-success"));
    }


}
