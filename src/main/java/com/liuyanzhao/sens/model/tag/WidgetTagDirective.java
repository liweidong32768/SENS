package com.liuyanzhao.sens.model.tag;

import com.liuyanzhao.sens.model.enums.WidgetTypeEnum;
import com.liuyanzhao.sens.service.*;
import freemarker.core.Environment;
import freemarker.template.*;
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
public class WidgetTagDirective implements TemplateDirectiveModel {

    private static final String METHOD_KEY = "method";

    @Autowired
    private WidgetService widgetService;

    @Override
    public void execute(Environment environment, Map map, TemplateModel[] templateModels, TemplateDirectiveBody templateDirectiveBody) throws TemplateException, IOException {
        DefaultObjectWrapperBuilder builder = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_25);
        if (map.containsKey(METHOD_KEY)) {
            String method = map.get(METHOD_KEY).toString();
            switch (method) {
                case "sidebarWidgets":
                    environment.setVariable("sidebarWidgets", builder.build().wrap(widgetService.findByWidgetType(WidgetTypeEnum.POST_DETAIL_SIDEBAR_WIDGET.getCode())));
                    break;
                case "footerWidgets":
                    environment.setVariable("footerWidgets", builder.build().wrap(widgetService.findByWidgetType(WidgetTypeEnum.FOOTER_WIDGET.getCode())));
                    break;
                default:
                    break;
            }
        }
        templateDirectiveBody.render(environment.getOut());
    }
}
