//package com.liuyanzhao.sens.web.controller.core;
//
//import cn.hutool.core.util.RandomUtil;
//import com.liuyanzhao.sens.entity.*;
//import com.liuyanzhao.sens.model.dto.AttachLocationEnum;
//import com.liuyanzhao.sens.model.dto.SensConst;
//import com.liuyanzhao.sens.model.dto.LogsRecord;
//import com.liuyanzhao.sens.model.enums.*;
//import com.liuyanzhao.sens.service.*;
//import com.liuyanzhao.sens.utils.Md5Util;
//import com.liuyanzhao.sens.utils.SensUtils;
//import cn.hutool.core.date.DateUtil;
//import cn.hutool.crypto.SecureUtil;
//import cn.hutool.extra.servlet.ServletUtil;
//import freemarker.template.Configuration;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.RandomUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import javax.servlet.http.HttpServletRequest;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * <pre>
// *     博客初始化控制器
// * </pre>
// *
// * @author : saysky
// * @date : 2018/1/28
// */
//@Slf4j
//@Controller
//@RequestMapping(value = "/install")
//public class InstallController {
//
//    @Autowired
//    private OptionsService optionsService;
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private LogService logService;
//
//    @Autowired
//    private PostService postService;
//
//    @Autowired
//    private CategoryService categoryService;
//
//    @Autowired
//    private CommentService commentService;
//
//    @Autowired
//    private MenuService menuService;
//
//    @Autowired
//    private Configuration configuration;
//
//    @Autowired
//    private RoleService roleService;
//
//    @Autowired
//    private UserRoleRefService userRoleRefService;
//
//    /**
//     * 渲染安装页面
//     *
//     * @param model model
//     * @return 模板路径
//     */
//    @GetMapping
//    public String install(Model model) {
//        try {
//            if (StringUtils.equals(TrueFalseEnum.TRUE.getDesc(), SensConst.OPTIONS.get(BlogPropertiesEnum.IS_INSTALL.getProp()))) {
//                model.addAttribute("isInstall", true);
//                return "redirect:/";
//            } else {
//                model.addAttribute("isInstall", false);
//            }
//        } catch (Exception e) {
//            log.error(e.getMessage());
//        }
//        return "common/install";
//    }
//
//    /**
//     * 执行安装
//     *
//     * @param blogLocale      系统语言
//     * @param blogTitle       博客标题
//     * @param blogUrl         博客网址
//     * @param userName        用户名
//     * @param userDisplayName 用户名显示名
//     * @param userEmail       用户邮箱
//     * @param userPwd         用户密码
//     * @param request         request
//     * @return true：安装成功，false：安装失败
//     */
//    @PostMapping(value = "/do")
//    @ResponseBody
//    public boolean doInstall(@RequestParam("blogLocale") String blogLocale,
//                             @RequestParam("blogTitle") String blogTitle,
//                             @RequestParam("blogUrl") String blogUrl,
//                             @RequestParam("userName") String userName,
//                             @RequestParam("userDisplayName") String userDisplayName,
//                             @RequestParam("userEmail") String userEmail,
//                             @RequestParam("userPwd") String userPwd,
//                             HttpServletRequest request) {
//        try {
//            if (StringUtils.equals(TrueFalseEnum.TRUE.getDesc(), SensConst.OPTIONS.get(BlogPropertiesEnum.IS_INSTALL.getProp()))) {
//                return false;
//            }
//            //创建新的用户
//            User user = new User();
//            user.setUserName(userName);
//            if (StringUtils.isBlank(userDisplayName)) {
//                userDisplayName = userName;
//            }
//            user.setUserAvatar("/static/images/avatar/" + RandomUtils.nextInt(1, 41) + ".jpeg");
//            user.setUserDisplayName(userDisplayName);
//            user.setUserEmail(userEmail);
//            user.setUserPass(Md5Util.toMd5(userPwd, "sens", 10));
//            userService.insertUser(user);
//
//            //添加权限
//            Role role = roleService.findByRoleName(RoleEnum.ADMIN.getDesc());
//            if (role != null) {
//                userRoleRefService.saveByUserRoleRef(new UserRoleRef(user.getUserId(), role.getId()));
//            }
//
//            //默认分类
//            Category category = new Category();
//            category.setCatePid(0L);
//            category.setCateName("未分类");
//            category.setCateUrl("default");
//            category.setCateDesc("未分类");
//            categoryService.saveByCategory(category);
//
//            //第一篇文章
//            Post post = new Post();
//            List<Category> categories = new ArrayList<>();
//            categories.add(category);
//            post.setPostTitle("Hello SENS!");
//            post.setPostContent("<h1 id=\"h1-hello-sens-\"><a name=\"Hello SENS!\" class=\"reference-link\"></a><span class=\"header-link octicon octicon-link\"></span>Hello SENS!</h1><p>欢迎使用SENS进行创作，删除这篇文章后赶紧开始吧。</p>\n");
//            post.setPostSummary("欢迎使用SENS进行创作，删除这篇文章后赶紧开始吧。");
//            post.setPostStatus(0);
//            post.setPostDate(DateUtil.date());
//            post.setPostUrl("hello-sens");
//            post.setUserId(user.getUserId());
//            post.setCategories(categories);
//            post.setAllowComment(AllowCommentEnum.ALLOW.getCode());
//            post.setPostThumbnail("/static/images/thumbnail/img_" + RandomUtil.randomInt(0, 14) + ".jpg");
//            postService.saveByPost(post);
//
//            //第一个评论
//            Comment comment = new Comment();
//            comment.setPostId(post.getPostId());
//            comment.setCommentAuthor("saysky");
//            comment.setCommentAuthorEmail("admin@liuyanzhao.com");
//            comment.setCommentAuthorUrl("https://liuyanzhao.com");
//            comment.setCommentAuthorIp("127.0.0.1");
//            comment.setCommentAuthorEmailMd5("7cc7f29278071bd4dce995612d428834");
//            comment.setCommentDate(DateUtil.date());
//            comment.setCommentContent("欢迎，欢迎！");
//            comment.setCommentStatus(0);
//            comment.setCommentAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.162 Safari/537.36");
//            comment.setIsAdmin(0);
//            commentService.saveByComment(comment);
//
//
//            final Map<String, String> options = new HashMap<>();
//            options.put(BlogPropertiesEnum.IS_INSTALL.getProp(), TrueFalseEnum.TRUE.getDesc());
//            options.put(BlogPropertiesEnum.BLOG_LOCALE.getProp(), blogLocale);
//            options.put(BlogPropertiesEnum.BLOG_TITLE.getProp(), blogTitle);
//            options.put(BlogPropertiesEnum.BLOG_URL.getProp(), blogUrl);
//            options.put(BlogPropertiesEnum.THEME.getProp(), "begin");
//            options.put(BlogPropertiesEnum.BLOG_START.getProp(), DateUtil.format(DateUtil.date(), "yyyy-MM-dd"));
//            options.put(BlogPropertiesEnum.SMTP_EMAIL_ENABLE.getProp(), TrueFalseEnum.FALSE.getDesc());
//            options.put(BlogPropertiesEnum.NEW_COMMENT_NOTICE.getProp(), TrueFalseEnum.FALSE.getDesc());
//            options.put(BlogPropertiesEnum.COMMENT_PASS_NOTICE.getProp(), TrueFalseEnum.FALSE.getDesc());
//            options.put(BlogPropertiesEnum.COMMENT_REPLY_NOTICE.getProp(), TrueFalseEnum.FALSE.getDesc());
//            options.put(BlogPropertiesEnum.ATTACH_LOC.getProp(), AttachLocationEnum.SERVER.getDesc());
//            optionsService.saveOptions(options);
//
//            //更新日志
//            logService.saveByLog(
//                new Log(
//                        LogsRecord.INSTALL,
//                        "安装成功，欢迎使用SENS。",
//                        ServletUtil.getClientIP(request),
//                        DateUtil.date()
//                )
//            );
//
//            Menu menuIndex = new Menu();
//            menuIndex.setMenuName("首页");
//            menuIndex.setMenuUrl("/");
//            menuIndex.setMenuSort(1);
//            menuIndex.setMenuIcon("");
//            menuIndex.setMenuType(MenuTypeEnum.FRONT_MAIN_MENU.getCode());
//            menuService.saveByMenu(menuIndex);
//
//            Menu menuArchive = new Menu();
//            menuArchive.setMenuName("归档");
//            menuArchive.setMenuUrl("/article");
//            menuArchive.setMenuSort(2);
//            menuArchive.setMenuIcon("");
//            menuArchive.setMenuType(MenuTypeEnum.FRONT_TOP_MENU.getCode());
//            menuService.saveByMenu(menuArchive);
//
//            SensConst.OPTIONS.clear();
//            SensConst.OPTIONS = optionsService.findAllOptions();
//
//            configuration.setSharedVariable("options", optionsService.findAllOptions());
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            return false;
//        }
//        return true;
//    }
//}
