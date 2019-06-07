package com.liuyanzhao.sens.web.controller.admin;

import com.baomidou.mybatisplus.plugins.Page;
import com.google.common.base.Strings;
import com.liuyanzhao.sens.entity.*;
import com.liuyanzhao.sens.model.dto.JsonResult;
import com.liuyanzhao.sens.model.dto.LogsRecord;
import com.liuyanzhao.sens.model.dto.SensConst;
import com.liuyanzhao.sens.model.dto.UserToken;
import com.liuyanzhao.sens.model.enums.*;
import com.liuyanzhao.sens.service.*;
import com.liuyanzhao.sens.utils.LocaleMessageUtil;
import com.liuyanzhao.sens.utils.Md5Util;
import com.liuyanzhao.sens.utils.Response;
import com.liuyanzhao.sens.web.controller.core.BaseController;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.HtmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.lang.management.*;
import java.util.*;

/**
 * <pre>
 *     后台首页控制器
 * </pre>
 *
 * @author : saysky
 * @date : 2017/12/5
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin")
public class AdminController extends BaseController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private LogService logService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private UserRoleRefService userRoleRefService;


    @Autowired(required = false)
    private HttpServletRequest request;

    @Autowired
    private CommentService commentService;

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private LocaleMessageUtil localeMessageUtil;

    @Autowired
    private MailService mailService;

    /**
     * 请求后台页面
     *
     * @param model model
     * @return 模板路径admin/admin_index
     */
    @GetMapping(value = {"", "/index"})
    @RequiresPermissions("dashboard")
    public String index(Model model) {

        //查询评论的条数
        Integer commentCount = commentService.countByStatus(null);
        model.addAttribute("commentCount", commentCount);

        //查询最新的文章
        List<Post> postsLatest = postService.findPostLatest();
        model.addAttribute("postTopFive", postsLatest);

        //查询最新的日志
        List<Log> logsLatest = logService.findLogLatest();
        model.addAttribute("logs", logsLatest);

        //查询最新的评论，五条
        List<Comment> comments = commentService.findCommentsLatest(10);
        if (comments.size() > 5) {
            comments = comments.subList(0, 5);
        }
        model.addAttribute("comments", comments);

        //附件数量
        model.addAttribute("mediaCount", attachmentService.getCount());

        //文章阅读总数
        Long postViewsSum = postService.getSumPostViews();
        model.addAttribute("postViewsSum", postViewsSum);
        return "admin/admin_index";
    }

    /**
     * 处理跳转到登录页的请求
     *
     * @return 模板路径admin/admin_login
     */
    @GetMapping(value = "/login")
    public String login() {
        Subject subject = SecurityUtils.getSubject();
        //如果已经登录，跳转到后台首页
        if (subject.isAuthenticated()) {
            return "redirect:/admin";
        }
        return "admin/admin_login";
    }

    /**
     * 验证登录信息
     *
     * @param loginName 登录名：邮箱／用户名
     * @param loginPwd  loginPwd 密码
     * @return JsonResult JsonResult
     */
    @PostMapping(value = "/getLogin")
    @ResponseBody
    public JsonResult getLogin(@ModelAttribute("loginName") String loginName,
                               @ModelAttribute("loginPwd") String loginPwd) {

        Subject subject = SecurityUtils.getSubject();
        UserToken token = new UserToken(loginName, loginPwd, LoginType.NORMAL.getDesc());
        try {
            subject.login(token);
            if (subject.isAuthenticated()) {
                //登录成功，修改登录错误次数为0
                User user = (User) subject.getPrincipal();
                userService.updateUserLoginNormal(user);

                logService.saveByLog(new Log(LogsRecord.LOGIN, LogsRecord.LOGIN_SUCCESS + "[" + user.getUserName() + "]", ServletUtil.getClientIP(request), DateUtil.date()));
                return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.login.success"));
            }
        } catch (UnknownAccountException e) {
            log.info("UnknownAccountException -- > 账号不存在：");
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.login.user.not.found"));
        } catch (IncorrectCredentialsException e) {
            //更新失败次数
            User user;
            if (Validator.isEmail(loginName)) {
                user = userService.findByEmail(loginName);
            } else {
                user = userService.findByUserName(loginName);
            }
            if (user != null) {
                Integer errorCount = userService.updateUserLoginError(user);
                //超过五次禁用账户
                if (errorCount >= CommonParamsEnum.FIVE.getValue()) {
                    userService.updateUserLoginEnable(user, TrueFalseEnum.FALSE.getDesc());
                }
                logService.saveByLog(
                        new Log(
                                LogsRecord.LOGIN,
//                                LogsRecord.LOGIN_ERROR + "[" + HtmlUtil.escape(loginName) + "," + HtmlUtil.escape(loginPwd) + "]",
                                LogsRecord.LOGIN_ERROR + "[" + HtmlUtil.escape(loginName) + "]",
                                ServletUtil.getClientIP(request),
                                DateUtil.date()
                        )
                );
                Object[] args = {(5 - errorCount) > 0 ? (5 - errorCount) : 0};
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.login.failed", args));
            }
        } catch (LockedAccountException e) {
            log.info("LockedAccountException -- > 账号被锁定");
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), e.getMessage());
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.query-failed"));
    }

    /**
     * 处理跳转到登录页的请求
     *
     * @return 模板路径admin/admin_login
     */
    @GetMapping(value = "/register")
    public String register() {
        Subject subject = SecurityUtils.getSubject();
        //如果已经登录，跳转到后台首页
        if (subject.isAuthenticated()) {
            return "redirect:/admin";
        }
        return "admin/admin_register";
    }


    /**
     * 验证注册信息
     *
     * @param userName  用户名
     * @param userEmail 邮箱
     * @return JsonResult JsonResult
     */
    @PostMapping(value = "/getRegister")
    @ResponseBody
    public JsonResult getRegister(@ModelAttribute("userName") String userName,
                                  @ModelAttribute("userPass") String userPass,
                                  @ModelAttribute("userEmail") String userEmail) {
        if (StringUtils.equals(SensConst.OPTIONS.get(BlogPropertiesEnum.OPEN_REGISTER.getProp()), TrueFalseEnum.FALSE.getDesc())) {
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.register.close"));
        }
        //1.检查用户名
        User checkUser = userService.findByUserName(userName);
        if (checkUser != null) {
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.user.user-name-exist"));
        }
        //2.检查用户名
        User checkEmail = userService.findByEmail(userEmail);
        if (checkEmail != null) {
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.user.user-email-exist"));
        }
        //3.创建用户
        User user = new User();
        user.setUserName(userName);
        user.setUserDisplayName(userName);
        user.setUserEmail(userEmail);
        user.setEmailEnable(TrueFalseEnum.FALSE.getDesc());
        user.setLoginEnable(TrueFalseEnum.TRUE.getDesc());
        user.setLoginError(0);
        user.setUserPass(userPass);
        user.setUserAvatar("/static/images/avatar/" + RandomUtils.nextInt(1, 41) + ".jpeg");
        user.setStatus(UserStatusEnum.NORMAL.getCode());
        Response<User> response = userService.saveByUser(user);
        if (!response.isSuccess()) {
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), response.getMessage());
        }

        //4.关联角色
        String defaultRole = SensConst.OPTIONS.get(BlogPropertiesEnum.DEFAULT_REGISTER_ROLE.getProp());
        Role role = null;
        if(!Strings.isNullOrEmpty(defaultRole)) {
            role = roleService.findByRoleName(defaultRole);
        }
        if(role == null) {
            role = roleService.findByRoleName(RoleEnum.SUBSCRIBER.getDesc());
        }
        if (role != null) {
            userRoleRefService.saveByUserRoleRef(new UserRoleRef(user.getUserId(), role.getId()));
        }

        //4.发送激活验证码
//        if (StringUtils.equals(SensConst.OPTIONS.get(BlogPropertiesEnum.SMTP_EMAIL_ENABLE.getProp()), TrueFalseEnum.TRUE.getDesc())) {
//            Map<String, Object> map = new HashMap<>(8);
//            map.put("blogTitle", SensConst.OPTIONS.get(BlogPropertiesEnum.BLOG_TITLE.getProp()));
//            map.put("blogUrl", SensConst.OPTIONS.get(BlogPropertiesEnum.BLOG_URL.getProp()));
//            map.put("activeUrl", SensConst.OPTIONS.get(BlogPropertiesEnum.BLOG_URL.getProp()));
//            mailService.sendTemplateMail(
//                    userEmail,  SensConst.OPTIONS.get(BlogPropertiesEnum.BLOG_TITLE.getProp()) + "账户 - 电子邮箱激活", map, "common/mail_template/mail_active_email.ftl");
//        } else {
//            return new JsonResult(ResultCodeEnum.FAIL.getCode(), "本站没有启动SMTP，无法发送邮件！");
//        }

        logService.saveByLog(new Log(LogsRecord.REGISTER, user.getUserName(), ServletUtil.getClientIP(request), DateUtil.date()));
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.register.success"));
    }

    /**
     * 处理跳转忘记密码的请求
     *
     * @return 模板路径admin/admin_login
     */
    @GetMapping(value = "/forget")
    public String forget() {
        Subject subject = SecurityUtils.getSubject();
        //如果已经登录，跳转到后台首页
        if (subject.isAuthenticated()) {
            return "redirect:/admin";
        }
        return "admin/admin_forget";
    }

    /**
     * 处理忘记密码
     *
     * @param userName  用户名
     * @param userEmail 邮箱
     * @return JsonResult
     */
    @PostMapping(value = "/getForget")
    @ResponseBody
    public JsonResult getForget(@ModelAttribute("userName") String userName,
                                @ModelAttribute("userEmail") String userEmail) {

        try {
            User user = userService.findByUserName(userName);
            if (user != null && Objects.equals(user.getUserEmail(), userEmail)) {
                //验证成功，将密码由邮件方法发送给对方
                //1.修改密码
                String password = RandomStringUtils.randomNumeric(8);
                userService.updatePassword(user.getUserId(), Md5Util.toMd5(password, "sens", 10));
                //2.发送邮件
                if (StringUtils.equals(SensConst.OPTIONS.get(BlogPropertiesEnum.SMTP_EMAIL_ENABLE.getProp()), TrueFalseEnum.TRUE.getDesc())) {
                    Map<String, Object> map = new HashMap<>(8);
                    map.put("blogTitle", SensConst.OPTIONS.get(BlogPropertiesEnum.BLOG_TITLE.getProp()));
                    map.put("blogUrl", SensConst.OPTIONS.get(BlogPropertiesEnum.BLOG_URL.getProp()));
                    map.put("password", password);
                    mailService.sendTemplateMail(
                            userEmail, SensConst.OPTIONS.get(BlogPropertiesEnum.BLOG_TITLE.getProp()) + "账户 - 找回密码", map, "common/mail_template/mail_forget.ftl");
                } else {
                    return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.smtp-not-enable"));
                }
            } else {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.forget.username-email-invalid"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.operation-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.forget.password-send-mailbox"));
    }

    /**
     * 退出登录
     *
     * @return 重定向到/admin/login
     */
    @GetMapping(value = "/logOut")
    public String logOut() {
        Subject subject = SecurityUtils.getSubject();
        User user = (User) subject.getPrincipal();
        subject.logout();

        logService.saveByLog(new Log(LogsRecord.LOGOUT, user.getUserName(), ServletUtil.getClientIP(request), DateUtil.date()));
        log.info("用户[{}]退出登录", user.getUserName());
        return "redirect:/admin/login";
    }

    /**
     * 查看所有日志
     *
     * @param model model model
     * @param page  page 当前页码
     * @param size  size 每页条数
     * @return 模板路径admin/widget/_logs-all
     */
    @GetMapping(value = "/logs")
    @RequiresPermissions("system:logs")
    public String logs(Model model,
                       @RequestParam(value = "page", defaultValue = "0") Integer page,
                       @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Page pageable = new Page(page, size);
        Page<Log> logs = logService.findAllLog(pageable);
        model.addAttribute("logs", logs);
        return "admin/widget/_logs-all";
    }

    /**
     * 清除所有日志
     *
     * @return 重定向到/admin
     */
    @GetMapping(value = "/logs/clear")
    @RequiresPermissions("system:logs-clear")
    public String logsClear() {
        try {
            logService.removeAllLog();
        } catch (Exception e) {
            log.error("清除日志失败：{}" + e.getMessage());
        }
        return "redirect:/admin";
    }

    /**
     * 不可描述的页面
     *
     * @return 模板路径admin/admin_sens
     */
    @GetMapping(value = "/sens")
    public String sens() {
        return "admin/admin_sens";
    }


    /**
     * 获得当前用户的菜单
     *
     * @return
     */
    @GetMapping(value = "/currentMenus")
    @ResponseBody
    public JsonResult getMenu() {
        Subject subject = SecurityUtils.getSubject();
        User user = (User) subject.getPrincipal();
        List<Menu> menus = Collections.emptyList();
        try {
            List<Role> roles = roleService.listRolesByUserId(user.getUserId());
            Optional<Role> optional = roles.stream().min(Comparator.comparingInt(Role::getLevel));
            Role role = optional.get();
            String language = SensConst.OPTIONS.get(BlogPropertiesEnum.BLOG_LOCALE.getProp());
            if (!StringUtils.isBlank(language)) {
                menus = menuService.getMenuByRoleId(role.getId(), language);
            } else {
                menus = menuService.getMenuByRoleId(role.getId(), LanguageType.CHINESE.getValue());
            }
        } catch (Exception e) {
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), "");
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), "", menus);
    }

    /**
     * 查看memory
     *
     * @return
     */
    @GetMapping("/memory")
    @ResponseBody
    public String memory() {

        StringBuilder sb = new StringBuilder();

        MemoryMXBean memorymbean = ManagementFactory.getMemoryMXBean();
        MemoryUsage usage = memorymbean.getHeapMemoryUsage();
        sb.append("INIT HEAP: " + usage.getInit() / 1024 / 2024 + "MB\n");
        sb.append("MAX HEAP: " + usage.getMax() / 1024 / 2024 + "MB\n");
        sb.append("USE HEAP: " + usage.getUsed() / 1024 / 2024 + "MB\n");
        sb.append("\nFull Information:");
        sb.append("Heap Memory Usage: "
                + memorymbean.getHeapMemoryUsage() + "\n");
        sb.append("Non-Heap Memory Usage: "
                + memorymbean.getNonHeapMemoryUsage() + "\n");

        List<String> inputArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
        sb.append("===================java options=============== \n");
        sb.append(inputArguments + "\n");


        sb.append("=======================通过java来获取相关系统状态============================ \n");
        //Java 虚拟机中的内存总量,以字节为单位
        int i = (int) Runtime.getRuntime().totalMemory() / 1024 / 1024;
        sb.append("总的内存量：" + i + "MB\n");
        //Java 虚拟机中的空闲内存量
        int j = (int) Runtime.getRuntime().freeMemory() / 1024 / 1024;
        sb.append("空闲内存量：" + j + "MB\n");
        sb.append("最大内存量： " + Runtime.getRuntime().maxMemory() / 1024 / 1024 + "MB\n");

        sb.append("=======================OperatingSystemMXBean============================ \n");
        OperatingSystemMXBean osm = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        //获取操作系统相关信息
        sb.append("osm.getArch() " + osm.getArch() + "\n");
        sb.append("osm.getAvailableProcessors() " + osm.getAvailableProcessors() + "\n");
        sb.append("osm.getName() " + osm.getName() + "\n");
        sb.append("osm.getVersion() " + osm.getVersion() + "\n");
        //获取整个虚拟机内存使用情况
        sb.append("=======================MemoryMXBean============================ \n");
        MemoryMXBean mm = (MemoryMXBean) ManagementFactory.getMemoryMXBean();
        sb.append("getHeapMemoryUsage " + mm.getHeapMemoryUsage() + "\n");
        sb.append("getNonHeapMemoryUsage " + mm.getNonHeapMemoryUsage() + "\n");
        //获取各个线程的各种状态，CPU 占用情况，以及整个系统中的线程状况
        sb.append("=======================ThreadMXBean============================ \n");
        ThreadMXBean tm = (ThreadMXBean) ManagementFactory.getThreadMXBean();
        sb.append("getThreadCount " + tm.getThreadCount() + "\n");
        sb.append("getPeakThreadCount " + tm.getPeakThreadCount() + "\n");
        sb.append("getCurrentThreadCpuTime " + tm.getCurrentThreadCpuTime() + "\n");
        sb.append("getDaemonThreadCount " + tm.getDaemonThreadCount() + "\n");
        sb.append("getCurrentThreadUserTime " + tm.getCurrentThreadUserTime() + "\n");

        //当前编译器情况
        sb.append("=======================CompilationMXBean============================ \n");
        CompilationMXBean gm = (CompilationMXBean) ManagementFactory.getCompilationMXBean();
        sb.append("getName " + gm.getName() + "\n");
        sb.append("getTotalCompilationTime " + gm.getTotalCompilationTime() + "\n");

        //获取多个内存池的使用情况
        sb.append("=======================MemoryPoolMXBean============================ \n");
        List<MemoryPoolMXBean> mpmList = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean mpm : mpmList) {
            sb.append("getUsage " + mpm.getUsage() + "\n");
            sb.append("getMemoryManagerNames " + mpm.getMemoryManagerNames().toString() + "\n");
        }
        //获取GC的次数以及花费时间之类的信息
        sb.append("=======================MemoryPoolMXBean============================ \n");
        List<GarbageCollectorMXBean> gcmList = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gcm : gcmList) {
            sb.append(gcm.getName() + "\n");
        }
        //获取运行时信息
        sb.append("=======================RuntimeMXBean============================ \n");
        RuntimeMXBean rmb = (RuntimeMXBean) ManagementFactory.getRuntimeMXBean();
        sb.append("getClassPath " + rmb.getClassPath() + "\n");
        sb.append("getLibraryPath " + rmb.getLibraryPath() + "\n");
        sb.append("getVmVersion " + rmb.getVmVersion() + "\n");

        return sb.toString();
    }
}
