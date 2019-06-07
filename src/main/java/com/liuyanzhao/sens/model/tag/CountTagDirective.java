package com.liuyanzhao.sens.model.tag;

import com.liuyanzhao.sens.model.enums.PostStatusEnum;
import com.liuyanzhao.sens.model.enums.PostTypeEnum;
import com.liuyanzhao.sens.service.*;
import freemarker.core.Environment;
import freemarker.template.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * <pre>
 *     FreeMarker自定义标签,关于统计的
 * </pre>
 *
 * @author : saysky
 * @date : 2018/4/26
 */
@Component
public class CountTagDirective implements TemplateDirectiveModel {

    private static final String METHOD_KEY = "method";

    @Autowired
    private MenuService menuService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private TagService tagService;

    @Autowired
    private LinkService linkService;

    @Autowired
    private PostService postService;

    @Override
    public void execute(Environment environment, Map map, TemplateModel[] templateModels, TemplateDirectiveBody templateDirectiveBody) throws TemplateException, IOException {
        DefaultObjectWrapperBuilder builder = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_25);
        if (map.containsKey(METHOD_KEY)) {
            String method = map.get(METHOD_KEY).toString();
            switch (method) {
                case "postsCount":
                    environment.setVariable("postsCount", builder.build().wrap(postService.countByPostTypeAndStatus(PostTypeEnum.POST_TYPE_POST.getDesc(), PostStatusEnum.PUBLISHED.getCode())));
                    break;
                case "commentsCount":
                    environment.setVariable("commentsCount", builder.build().wrap(commentService.countByStatus(PostStatusEnum.PUBLISHED.getCode())));
                    break;
                case "tagsCount":
                    environment.setVariable("tagsCount", builder.build().wrap(tagService.getCount()));
                    break;
                case "categoriesCount":
                    environment.setVariable("categoriesCount", builder.build().wrap(categoryService.getSumCount()));
                    break;
                case "linksCount":
                    environment.setVariable("linksCount", builder.build().wrap(linkService.getCount()));
                    break;
                case "viewsCount":
                    environment.setVariable("viewsCount", builder.build().wrap(postService.getSumPostViews()));
                    break;
                case "lastUpdateTime":
                    environment.setVariable("lastUpdateTime", builder.build().wrap(postService.getLastUpdateTime()));
                    break;
                default:
                    break;
            }
        }
        templateDirectiveBody.render(environment.getOut());
    }
}
