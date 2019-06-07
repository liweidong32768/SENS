package com.liuyanzhao.sens.model.tag;

import com.liuyanzhao.sens.entity.User;
import com.liuyanzhao.sens.model.enums.CommentStatusEnum;
import com.liuyanzhao.sens.model.enums.MenuTypeEnum;
import com.liuyanzhao.sens.model.enums.SlideTypeEnum;
import com.liuyanzhao.sens.service.*;
import freemarker.core.Environment;
import freemarker.template.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * <pre>
 *     FreeMarker自定义标签
 * </pre>
 *
 * @author : saysky
 * @date : 2018/4/26
 */
@Component
public class AdminTagDirective implements TemplateDirectiveModel {

    private static final String METHOD_KEY = "method";

    @Autowired
    private CommentService commentService;

    @Override
    public void execute(Environment environment, Map map, TemplateModel[] templateModels, TemplateDirectiveBody templateDirectiveBody) throws TemplateException, IOException {
        Subject subject = SecurityUtils.getSubject();
        User user = (User) subject.getPrincipal();
        DefaultObjectWrapperBuilder builder = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_25);
        if (map.containsKey(METHOD_KEY)) {
            String method = map.get(METHOD_KEY).toString();
            switch (method) {
                case "newComments":
                    environment.setVariable("newComments", builder.build().wrap(commentService.findByAcceptUserAndStatus(user.getUserId(), CommentStatusEnum.CHECKING.getCode())));
                    break;
                default:
                    break;
            }
        }
        templateDirectiveBody.render(environment.getOut());
    }
}
