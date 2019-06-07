package com.liuyanzhao.sens.web.controller.admin;

import com.liuyanzhao.sens.entity.Log;
import com.liuyanzhao.sens.model.dto.SensConst;
import com.liuyanzhao.sens.model.dto.JsonResult;
import com.liuyanzhao.sens.model.dto.LogsRecord;
import com.liuyanzhao.sens.model.enums.BlogPropertiesEnum;
import com.liuyanzhao.sens.model.enums.ResultCodeEnum;
import com.liuyanzhao.sens.model.enums.TrueFalseEnum;
import com.liuyanzhao.sens.service.LogService;
import com.liuyanzhao.sens.service.OptionsService;
import com.liuyanzhao.sens.utils.SensUtils;
import com.liuyanzhao.sens.utils.LocaleMessageUtil;
import com.liuyanzhao.sens.web.controller.core.BaseController;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.extra.servlet.ServletUtil;
import freemarker.template.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * <pre>
 *     后台主题管理控制器
 * </pre>
 *
 * @author : saysky
 * @date : 2017/12/16
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/theme")
@RequiresPermissions("appearence:theme*")
public class  ThemeController extends BaseController {

    @Autowired
    private OptionsService optionsService;

    @Autowired
    private LogService logService;

    @Autowired
    private Configuration configuration;

    @Autowired
    private LocaleMessageUtil localeMessageUtil;

    private static final String NOT_FOUND_GIT = "-bash: git: command not found";

    /**
     * 渲染主题设置页面
     *
     * @param model model
     * @return 模板路径admin/admin_theme
     */
    @GetMapping
    public String themes(Model model) {
        model.addAttribute("activeTheme", BaseController.THEME);
        if (null != SensConst.THEMES) {
            model.addAttribute("themes", SensConst.THEMES);
        }
        return "admin/admin_theme";
    }

    /**
     * 激活主题
     *
     * @param siteTheme 主题名称
     * @param request   request
     * @return JsonResult
     */
    @GetMapping(value = "/set")
    @ResponseBody
    public JsonResult activeTheme(@RequestParam("siteTheme") String siteTheme,
                                  HttpServletRequest request) {
        try {
            //保存主题设置项
            optionsService.saveOption(BlogPropertiesEnum.THEME.getProp(), siteTheme);
            //设置主题
            BaseController.THEME = siteTheme;
            SensConst.OPTIONS.clear();
            SensConst.OPTIONS = optionsService.findAllOptions();
            configuration.setSharedVariable("themeName", siteTheme);
            configuration.setSharedVariable("options", SensConst.OPTIONS);
            log.info("已将主题改变为：{}", siteTheme);
            logService.saveByLog(
                    new Log(LogsRecord.CHANGE_THEME, "更换为" + siteTheme, ServletUtil.getClientIP(request), DateUtil.date())
            );
            return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.theme.change-success", new Object[]{siteTheme}));
        } catch (Exception e) {
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.theme.change-failed"));
        }
    }


    /**
     * 上传主题
     *
     * @param file 文件
     * @return JsonResult
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult uploadTheme(@RequestParam("file") MultipartFile file,
                                  HttpServletRequest request) {
        try {
            if (!file.isEmpty()) {
                //获取项目根路径
                File basePath = new File(ResourceUtils.getURL("classpath:").getPath());
                File themePath = new File(basePath.getAbsolutePath(), new StringBuffer("templates/themes/").append(file.getOriginalFilename()).toString());
                file.transferTo(themePath);
                log.info("上传主题成功，路径：" + themePath.getAbsolutePath());
                logService.saveByLog(
                        new Log(LogsRecord.UPLOAD_THEME, file.getOriginalFilename(), ServletUtil.getClientIP(request), DateUtil.date())
                );
                ZipUtil.unzip(themePath, new File(basePath.getAbsolutePath(), "templates/themes/"));
                FileUtil.del(themePath);
                SensConst.THEMES.clear();
                SensConst.THEMES = SensUtils.getThemes();
            } else {
                log.error("上传主题失败，没有选择文件");
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.theme.upload-no-file"));
            }
        } catch (Exception e) {
            log.error("上传主题失败：{}", e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.theme.upload-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.theme.upload-success"));
    }

    /**
     * 删除主题
     *
     * @param themeName 主题文件夹名
     * @return string 重定向到/admin/theme
     */
    @GetMapping(value = "/remove")
    public String removeTheme(@RequestParam("themeName") String themeName) {
        try {
            File basePath = new File(ResourceUtils.getURL("classpath:").getPath());
            File themePath = new File(basePath.getAbsolutePath(), "templates/themes/" + themeName);
            FileUtil.del(themePath);
            SensConst.THEMES.clear();
            SensConst.THEMES = SensUtils.getThemes();
        } catch (Exception e) {
            log.error("删除主题失败：{}", e.getMessage());
        }
        return "redirect:/admin/theme";
    }

    /**
     * 安装主题弹出层
     *
     * @return 目录路径admin/widget/_theme-install
     */
    @GetMapping(value = "/install")
    public String install() {
        return "admin/widget/_theme-install";
    }

    /**
     * 在线拉取主题
     *
     * @param remoteAddr 远程地址
     * @param themeName  主题名称
     * @return JsonResult
     */
    @PostMapping(value = "/clone")
    @ResponseBody
    public JsonResult cloneFromRemote(@RequestParam(value = "remoteAddr") String remoteAddr,
                                      @RequestParam(value = "themeName") String themeName) {
        if (StringUtils.isBlank(remoteAddr) || StringUtils.isBlank(themeName)) {
            return new JsonResult(0, localeMessageUtil.getMessage("code.admin.common.info-no-complete"));
        }
        try {
            File basePath = new File(ResourceUtils.getURL("classpath:").getPath());
            File themePath = new File(basePath.getAbsolutePath(), "templates/themes");
            String cmdResult = RuntimeUtil.execForStr("git clone " + remoteAddr + " " + themePath.getAbsolutePath() + "/" + themeName);
            if (NOT_FOUND_GIT.equals(cmdResult)) {
                return new JsonResult(0, localeMessageUtil.getMessage("code.admin.theme.no-git"));
            }
            SensConst.THEMES.clear();
            SensConst.THEMES = SensUtils.getThemes();
        } catch (FileNotFoundException e) {
            log.error("克隆主题失败：{}", e.getMessage());
            return new JsonResult(0, localeMessageUtil.getMessage("code.admin.theme.clone-theme-failed") + e.getMessage());
        }
        return new JsonResult(1, localeMessageUtil.getMessage("code.admin.common.install-success"));
    }

    /**
     * 更新主题
     *
     * @param themeName 主题名
     * @return JsonResult
     */
    @GetMapping(value = "/pull")
    @ResponseBody
    public JsonResult pullFromRemote(@RequestParam(value = "themeName") String themeName) {
        try {
            File basePath = new File(ResourceUtils.getURL("classpath:").getPath());
            File themePath = new File(basePath.getAbsolutePath(), "templates/themes");
            String cmdResult = RuntimeUtil.execForStr("cd " + themePath.getAbsolutePath() + "/" + themeName + " && git pull");
            if (NOT_FOUND_GIT.equals(cmdResult)) {
                return new JsonResult(0, localeMessageUtil.getMessage("code.admin.theme.no-git"));
            }
            SensConst.THEMES.clear();
            SensConst.THEMES = SensUtils.getThemes();
        } catch (Exception e) {
            log.error("更新主题失败：{}", e.getMessage());
            return new JsonResult(0, localeMessageUtil.getMessage("code.admin.theme.update-theme-failed") + e.getMessage());
        }
        return new JsonResult(1, localeMessageUtil.getMessage("code.admin.common.update-success"));
    }

    /**
     * 跳转到主题设置
     *
     * @param theme theme名称
     */
    @GetMapping(value = "/options")
    public String setting(Model model,
                          @RequestParam("theme") String theme,
                          @RequestParam("hasUpdate") String hasUpdate) {
        model.addAttribute("themeDir", theme);
        if (StringUtils.equals(hasUpdate, TrueFalseEnum.TRUE.getDesc())) {
            model.addAttribute("hasUpdate", true);
        } else {
            model.addAttribute("hasUpdate", false);
        }
        return "themes/" + theme + "/module/options";
    }

    /**
     * 编辑主题
     *
     * @param model model
     * @return 模板路径admin/admin_theme-editor
     */
    @GetMapping(value = "/editor")
    public String editor(Model model) {
        List<String> tpls = SensUtils.getTplName(BaseController.THEME);
        model.addAttribute("tpls", tpls);
        return "admin/admin_theme-editor";
    }

    /**
     * 获取模板文件内容
     *
     * @param tplName 模板文件名
     * @return 模板内容
     */
    @GetMapping(value = "/getTpl", produces = "text/text;charset=UTF-8")
    @ResponseBody
    public String getTplContent(@RequestParam("tplName") String tplName) {
        String tplContent = "";
        try {
            //获取项目根路径
            File basePath = new File(ResourceUtils.getURL("classpath:").getPath());
            //获取主题路径
            File themesPath = new File(basePath.getAbsolutePath(), new StringBuffer("templates/themes/").append(BaseController.THEME).append("/").append(tplName).toString());
            FileReader fileReader = new FileReader(themesPath);
            tplContent = fileReader.readString();
        } catch (Exception e) {
            log.error("获取模板文件错误：{}", e.getMessage());
        }
        return tplContent;
    }

    /**
     * 保存修改模板
     *
     * @param tplName    模板名称
     * @param tplContent 模板内容
     * @return JsonResult
     */
    @PostMapping(value = "/editor/save")
    @ResponseBody
    public JsonResult saveTpl(@RequestParam("tplName") String tplName,
                              @RequestParam("tplContent") String tplContent) {
        if (StringUtils.isBlank(tplContent)) {
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.theme.edit.no-content"));
        }
        try {
            //获取项目根路径
            File basePath = new File(ResourceUtils.getURL("classpath:").getPath());
            //获取主题路径
            File tplPath = new File(basePath.getAbsolutePath(), new StringBuffer("templates/themes/").append(BaseController.THEME).append("/").append(tplName).toString());
            FileWriter fileWriter = new FileWriter(tplPath);
            fileWriter.write(tplContent);
        } catch (Exception e) {
            log.error("模板保存失败：{}", e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.save-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.save-success"));
    }
}
