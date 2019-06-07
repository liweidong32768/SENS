package com.liuyanzhao.sens.web.controller.front;

import cn.hutool.core.date.DateUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.liuyanzhao.sens.entity.Log;
import com.liuyanzhao.sens.entity.ThirdAppBind;
import com.liuyanzhao.sens.entity.User;
import com.liuyanzhao.sens.model.dto.LogsRecord;
import com.liuyanzhao.sens.model.dto.SensConst;
import com.liuyanzhao.sens.model.dto.UserToken;
import com.liuyanzhao.sens.model.enums.BindTypeEnum;
import com.liuyanzhao.sens.model.enums.LoginType;
import com.liuyanzhao.sens.service.*;
import com.liuyanzhao.sens.utils.LocaleMessageUtil;
import com.liuyanzhao.sens.utils.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;


/**
 * @author 言曌
 * @date 2018/5/9 下午2:59
 */
@Controller
@Slf4j
public class FrontOAuthController {

    @Autowired
    private QQAuthService qqAuthService;

    @Autowired
    private GithubAuthService githubAuthService;

    @Autowired
    private UserService userService;

    @Autowired
    private ThirdAppBindService thirdAppBindService;

    @Autowired
    private LogService logService;

    @Autowired
    private LocaleMessageUtil localeMessageUtil;

    /**
     * 第三方授权后会回调此方法，并将code传过来
     *
     * @param code    回调code
     * @param request request
     * @param model   model
     * @return
     */
    @GetMapping("/oauth/qq/callback")
    public String oauthByQQ(@RequestParam(value = "code") String code, HttpServletRequest request, RedirectAttributes model) {
        Response<String> tokenResponse = qqAuthService.getAccessToken(code);
        if (tokenResponse.isSuccess()) {
            Response<String> openidResponse = qqAuthService.getOpenId(tokenResponse.getData());
            if (openidResponse.isSuccess()) {
                //根据openId去找关联的用户
                String openId = openidResponse.getData();
                ThirdAppBind bind = thirdAppBindService.findByAppTypeAndOpenId(BindTypeEnum.QQ.getValue(), openId);
                if (bind != null && bind.getUserId() != null) {
                    //执行Login操作
                    User user = userService.findByUserId(bind.getUserId());
                    if (user != null) {
                        Subject subject = SecurityUtils.getSubject();
                        UserToken userToken = new UserToken(user.getUserName(), user.getUserPass(), LoginType.FREE.getDesc());
                        try {
                            subject.login(userToken);
                        } catch (LockedAccountException e) {
                            e.printStackTrace();
                            log.error("第三方登录(QQ)免密码登录失败, 账号被锁定, cause:{}", e.getMessage());
                            model.addAttribute("error", e.getMessage());
                            return "redirect:/admin/login";
                        } catch (Exception e) {
                            e.printStackTrace();
                            log.error("第三方登录(QQ)免密码登录失败, cause:{}", e.getMessage());
                            model.addAttribute("error", localeMessageUtil.getMessage("code.admin.common.query-failed"));
                            return "redirect:/admin/login";
                        }
                        logService.saveByLog(new Log(LogsRecord.LOGIN, LogsRecord.LOGIN_SUCCESS + "(QQ登录)[" + user.getUserName() + "]", ServletUtil.getClientIP(request), DateUtil.date()));
                        log.info("用户[{}]登录成功(QQ登录)。", user.getUserDisplayName());
                        return "redirect:/admin";
                    }
                } else {
                    //1.如果登录了，就跳转到绑定
                    Subject subject = SecurityUtils.getSubject();
                    if (subject.isAuthenticated()) {
                        ThirdAppBind thirdAppBind = new ThirdAppBind();
                        thirdAppBind.setOpenId(openId);
                        thirdAppBind.setAppType(BindTypeEnum.QQ.getValue());
                        thirdAppBind.setCreateTime(new Date());
                        thirdAppBind.setStatus(1);
                        User user = (User) subject.getPrincipal();
                        thirdAppBind.setUserId(user.getUserId());
                        thirdAppBindService.saveByThirdAppBind(thirdAppBind);
                        return "redirect:/admin/user/profile";
                    }
                    //2.如果没有登录，跳转到注册
                    else {
                        return "redirect:/admin/register";
                    }
                }
            }
        }
        return "redirect:/admin/login";
    }

    /**
     * 第三方授权后会回调此方法，并将code传过来
     *
     * @param code code
     * @return
     */
    @GetMapping("/oauth/github/callback")
    public String oauthByGitHub(@RequestParam(value = "code") String code,
                                HttpServletRequest request) {
        Response<String> tokenResponse = githubAuthService.getAccessToken(code);
        if (tokenResponse.isSuccess()) {
            Response<String> openidResponse = githubAuthService.getOpenId(tokenResponse.getData());
            if (openidResponse.isSuccess()) {
                //根据openId去找关联的用户
                String openId = openidResponse.getData();
                ThirdAppBind bind = thirdAppBindService.findByAppTypeAndOpenId(BindTypeEnum.GITHUB.getValue(), openId);
                if (bind != null && bind.getUserId() != null) {
                    //执行Login操作
                    User user = userService.findByUserId(bind.getUserId());
                    if (user != null) {
                        Subject subject = SecurityUtils.getSubject();
                        UserToken userToken = new UserToken(user.getUserName(), user.getUserPass(), LoginType.FREE.getDesc());
                        try {
                            subject.login(userToken);
                        } catch (Exception e) {
                            e.printStackTrace();
                            log.error("第三方登录(GitHub)免密码登录失败, cause:{}", e);
                            return "redirect:/admin/login";
                        }
                        logService.saveByLog(new Log(LogsRecord.LOGIN, LogsRecord.LOGIN_SUCCESS + "(GitHub登录)[" + user.getUserName() + "]", ServletUtil.getClientIP(request), DateUtil.date()));
                        log.info("用户[{}]登录成功(登录)。", user.getUserDisplayName());
                        return "redirect:/admin";
                    }
                } else {
                    //1.如果登录了，就跳转到绑定
                    Subject subject = SecurityUtils.getSubject();
                    if (subject.isAuthenticated()) {
                        ThirdAppBind thirdAppBind = new ThirdAppBind();
                        thirdAppBind.setOpenId(openId);
                        thirdAppBind.setAppType(BindTypeEnum.GITHUB.getValue());
                        thirdAppBind.setCreateTime(new Date());
                        thirdAppBind.setStatus(1);
                        User user = (User) subject.getPrincipal();
                        thirdAppBind.setUserId(user.getUserId());
                        thirdAppBindService.saveByThirdAppBind(thirdAppBind);
                        return "redirect:/admin/user/profile";
                    }
                    //2.如果没有登录，跳转到注册
                    else {
                        return "redirect:/admin/register";
                    }
                }
            }
        }
        return "redirect:/admin/login";
    }

}