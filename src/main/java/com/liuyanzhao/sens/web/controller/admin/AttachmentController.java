package com.liuyanzhao.sens.web.controller.admin;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.plugins.Page;
import com.google.common.base.Strings;
import com.liuyanzhao.sens.entity.Attachment;
import com.liuyanzhao.sens.model.dto.AttachLocationEnum;
import com.liuyanzhao.sens.model.dto.JsonResult;
import com.liuyanzhao.sens.model.dto.LogsRecord;
import com.liuyanzhao.sens.model.enums.PostTypeEnum;
import com.liuyanzhao.sens.model.enums.ResultCodeEnum;
import com.liuyanzhao.sens.service.AttachmentService;
import com.liuyanzhao.sens.service.LogService;
import com.liuyanzhao.sens.utils.LocaleMessageUtil;
import com.liuyanzhao.sens.utils.SensUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.HashMap;
import java.util.Map;


/**
 * <pre>
 *     后台附件控制器
 * </pre>
 *
 * @author : saysky
 * @date : 2017/12/19
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/attachment")
public class AttachmentController {

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private LogService logService;

    @Autowired
    private LocaleMessageUtil localeMessageUtil;

    /**
     * 获取upload的所有图片资源并渲染页面
     *
     * @param model model
     * @return 模板路径admin/admin_attachment
     */
    @GetMapping
    @RequiresPermissions("attachment:list")
    public String attachments(Model model,
                              @RequestParam(value = "keywords", required = false) String keywords,
                              @RequestParam(value = "page", defaultValue = "1") Integer page,
                              @RequestParam(value = "size", defaultValue = "18") Integer size) {
        HashMap<String, Object> criteria = new HashMap<>();
        if (keywords != null && keywords != "") {
            criteria.put("keywords", keywords);
        }
        Page pageable = new Page(page, size);
        Page<Attachment> attachments = attachmentService.findAllAttachments(criteria, pageable);
        model.addAttribute("attachments", attachments);
        return "admin/admin_attachment";
    }

    /**
     * 处理获取附件详情的请求
     *
     * @param model    model
     * @param attachId 附件编号
     * @return 模板路径admin/widget/_attachment-detail
     */
    @GetMapping(value = "/detail")
    @RequiresPermissions("attachment:view")
    public String attachmentDetail(Model model, @RequestParam("attachId") Long attachId) {
        Attachment attachment = attachmentService.findByAttachId(attachId);
        model.addAttribute("attachment", attachment);
        if (attachment != null) {
            model.addAttribute("isPicture", SensUtils.isPicture(attachment.getAttachSuffix()));
        }
        return "admin/widget/_attachment-detail";
    }

    /**
     * 跳转选择附件页面
     *
     * @param model model
     * @param page  page 当前页码
     * @return 模板路径admin/widget/_attachment-select
     */
    @GetMapping(value = "/select")
    @RequiresPermissions("attachment:select")
    public String selectAttachment(Model model,
                                   @RequestParam(value = "keywords", required = false) String keywords,
                                   @RequestParam(value = "page", defaultValue = "0") Integer page,
                                   @RequestParam(value = "id", defaultValue = "none") String id,
                                   @RequestParam(value = "type", defaultValue = "normal") String type) {
        HashMap<String, Object> criteria = new HashMap<>();
        if (!Strings.isNullOrEmpty(keywords)) {
            criteria.put("keywords", keywords);
        }
        Page pageable = new Page(page, 18);
        Page<Attachment> attachments = null;
        try {
            attachments = attachmentService.findAllAttachments(criteria, pageable);
        } catch (Exception e) {
            log.error("附件获取失败：{}", e.getMessage());
        }
        model.addAttribute("attachments", attachments);
        model.addAttribute("id", id);
        if (StringUtils.equals(type, PostTypeEnum.POST_TYPE_POST.getDesc())) {
            return "admin/widget/_attachment-select-post";
        }
        return "admin/widget/_attachment-select";
    }

    /**
     * 下拉分页
     *
     * @param keywords 关键字查询
     * @param page     第几页
     * @param size     页大小
     * @return JSON
     */
    @GetMapping(value = "/page", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    @RequiresPermissions("attachment:page")
    public JsonResult listAttachmentByAjax(@RequestParam(value = "keywords", required = false) String keywords,
                                           @RequestParam(value = "page", defaultValue = "1") Integer page,
                                           @RequestParam(value = "size", defaultValue = "18") Integer size) {
        HashMap<String, Object> criteria = new HashMap<>();
        if (!Strings.isNullOrEmpty(keywords)) {
            criteria.put("keywords", keywords);
        }
        Page pageable = new Page(page, size);
        Page<Attachment> attachments;
        try {
            attachments = attachmentService.findAllAttachments(criteria, pageable);
        } catch (Exception e) {
            log.error("附件获取失败：{}", e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.query-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.query-success"), attachments.getRecords());
    }


    /**
     * 上传附件
     *
     * @param file    file
     * @param request request
     * @return Map
     */
    @PostMapping(value = "/upload", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    @RequiresPermissions("attachment:upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file,
                                      HttpServletRequest request) {
        return uploadAttachment(file, request);
    }


    public Map<String, Object> uploadAttachment(@RequestParam("file") MultipartFile file,
                                                HttpServletRequest request) {
        final Map<String, Object> result = new HashMap<>(3);
        if (!file.isEmpty()) {
            try {
                final Map<String, String> resultMap = attachmentService.upload(file, request);
                if (resultMap == null || resultMap.isEmpty()) {
                    log.error("File upload failed");
                    result.put("success", ResultCodeEnum.FAIL.getCode());
                    result.put("message", localeMessageUtil.getMessage("code.admin.attachment.upload-failed"));
                    return result;
                }
                //保存在数据库
                Attachment attachment = new Attachment();
                attachment.setAttachName(resultMap.get("fileName"));
                attachment.setAttachPath(resultMap.get("filePath"));
                attachment.setAttachSmallPath(resultMap.get("smallPath"));
                attachment.setAttachType(file.getContentType());
                attachment.setAttachSuffix(resultMap.get("suffix"));
                attachment.setAttachCreated(DateUtil.date());
                attachment.setAttachSize(resultMap.get("size"));
                attachment.setAttachWh(resultMap.get("wh"));
                attachment.setAttachLocation(resultMap.get("location"));
                attachmentService.saveByAttachment(attachment);
                log.info("Upload file {} to {} successfully", resultMap.get("fileName"), resultMap.get("filePath"));
                result.put("success", ResultCodeEnum.SUCCESS.getCode());
                result.put("message", localeMessageUtil.getMessage("code.admin.attachment.upload-success"));
                result.put("url", attachment.getAttachPath());
                result.put("filename", resultMap.get("filePath"));
                logService.saveByLog(LogsRecord.UPLOAD_FILE, resultMap.get("fileName"), request);
            } catch (Exception e) {
                log.error("Upload file failed:{}", e.getMessage());
                result.put("success", ResultCodeEnum.FAIL.getCode());
                result.put("message", localeMessageUtil.getMessage("code.admin.attachment.upload-failed"));
            }
        } else {
            log.error("File cannot be empty!");
        }
        return result;
    }

    /**
     * 移除附件的请求
     *
     * @param attachId 附件编号
     * @param request  request
     * @return JsonResult
     */
    @GetMapping(value = "/remove")
    @ResponseBody
    @RequiresPermissions("attachment:delete")
    public JsonResult removeAttachment(@RequestParam("attachId") Long attachId,
                                       HttpServletRequest request) {
        Attachment attachment = attachmentService.findByAttachId(attachId);
        String attachLocation = attachment.getAttachLocation();
        String delFileName = attachment.getAttachName();
        String delSmallFileName = attachment.getAttachName();
        boolean flag = true;
        try {
            //删除数据库中的内容
            attachmentService.remove(attachId);
            if (attachLocation != null) {
                if (attachLocation.equals(AttachLocationEnum.SERVER.getDesc())) {
                    //删除文件
                    String userPath = System.getProperties().getProperty("user.home") + "/sens";
                    File mediaPath = new File(userPath, attachment.getAttachPath().substring(0, attachment.getAttachPath().lastIndexOf('/')));
                    File delFile = new File(new StringBuffer(mediaPath.getAbsolutePath()).append("/").append(delFileName).toString());
                    File delSmallFile = new File(new StringBuffer(mediaPath.getAbsolutePath()).append("/").append(delSmallFileName).toString());
                    if (delFile.exists() && delFile.isFile()) {
                        flag = delFile.delete() && delSmallFile.delete();
                    }
                } else if (attachLocation.equals(AttachLocationEnum.QINIU.getDesc())) {
                    //七牛删除
                    String attachPath = attachment.getAttachPath();
                    int x = attachPath.lastIndexOf("uploads");
                    if (x > -1) {
                        String key = attachPath.substring(x);
                        flag = attachmentService.deleteQiNiuAttachment(key);
                    }
                } else if (attachLocation.equals(AttachLocationEnum.UPYUN.getDesc())) {
                    //又拍删除
                    String attachPath = attachment.getAttachPath();
                    String fileName = attachPath.substring(attachPath.lastIndexOf("/") + 1);
                    flag = attachmentService.deleteUpYunAttachment(fileName);
                } else {
                    //..
                }
            }
            if (flag) {
                log.info("Delete file {} successfully!", delFileName);
                logService.saveByLog(LogsRecord.REMOVE_FILE, delFileName, request);
            } else {
                log.error("Deleting attachment {} failed!", delFileName);
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.delete-failed"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Deleting attachment {} failed: {}", delFileName, e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.delete-failed"));
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.delete-success"));
    }

    /**
     * froala 编辑器上传图片，返回格式
     * {"link":"http://i.froala.com/images/missing.png"}
     *
     * @param file    file
     * @param request request
     * @return Map
     */
    @PostMapping(value = "/upload/froala", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    @RequiresPermissions("attachment:editor-upload")
    public Map<String, Object> editorUpload(@RequestParam("file") MultipartFile file,
                                            HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>(1);
        Map<String, Object> upload = uploadAttachment(file, request);
        result.put("link", upload.get("url"));
        return result;
    }

}
