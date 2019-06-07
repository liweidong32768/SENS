package com.liuyanzhao.sens.service.impl;

import cn.hutool.http.HtmlUtil;
import com.baomidou.mybatisplus.plugins.Page;
import com.liuyanzhao.sens.mapper.CommentMapper;
import com.liuyanzhao.sens.entity.Comment;
import com.liuyanzhao.sens.model.dto.SensConst;
import com.liuyanzhao.sens.model.enums.BlogPropertiesEnum;
import com.liuyanzhao.sens.model.enums.CommentStatusEnum;
import com.liuyanzhao.sens.model.enums.TrueFalseEnum;
import com.liuyanzhao.sens.service.CommentService;
import com.liuyanzhao.sens.service.PostService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *     评论业务逻辑实现类
 * </pre>
 *
 * @author : saysky
 * @date : 2018/1/22
 */
@Service
public class CommentServiceImpl implements CommentService {

    private static final String COMMENTS_CACHE_NAME = "comments";

    @Autowired(required = false)
    private CommentMapper commentMapper;

    @Autowired
    private PostService postService;

    @Override
    @CacheEvict(value = {COMMENTS_CACHE_NAME}, allEntries = true, beforeInvocation = true)
    @Transactional(rollbackFor = Exception.class)
    public void saveByComment(Comment comment) {
        if (comment != null && comment.getCommentId() != null) {
            commentMapper.updateById(comment);
        } else {
            commentMapper.insert(comment);
        }
        //修改文章评论数
        postService.updateCommentSize(comment.getPostId());
    }

    @Override
    @CacheEvict(value = {COMMENTS_CACHE_NAME}, allEntries = true, beforeInvocation = true)
    public void removeByCommentId(Long commentId) {
        Comment comment = this.findCommentById(commentId);
        if (comment != null) {
            //1.删除评论
            commentMapper.deleteById(commentId);
            //2.修改文章的评论数量
            postService.updateCommentSize(comment.getPostId());
        }


    }

    @Override
    public void removeByUserId(Long userId) {
        if (userId == null) {
            return;
        }
        commentMapper.deleteByUserId(userId);
    }

    @Override
    public Page<Comment> pagingByStatus(Integer status, Page<Comment> page) {
        return page.setRecords(commentMapper.pagingByCommentStatus(status, page));
    }

    @Override
    public Page<Comment> pagingBySendUserAndStatus(Long userId, Integer status, Page<Comment> page) {
        return page.setRecords(commentMapper.pagingByUserIdAndCommentStatus(userId, status, page));
    }

    @Override
    @Cacheable(value = COMMENTS_CACHE_NAME, key = "'comments_accept_uid_'+#userId+'_status_'+#status+'_page_'+#page.current")
    public Page<Comment> pagingByAcceptUserAndStatus(Long userId, Integer status, Page<Comment> page) {
        List<Comment> comments = commentMapper.pagingByAcceptUserIdAndCommentStatus(userId, status, page);
        return page.setRecords(comments);
    }

    @Override
    @Cacheable(value = COMMENTS_CACHE_NAME, key = "'comments_accept_uid_'+#userId+'_status_'+#status")
    public List<Comment> findByAcceptUserAndStatus(Long userId, Integer status) {
        Map<String, Object> map = new HashMap<>(2);
        map.put("user_id", userId);
        map.put("comment_status", status);
        List<Comment> comments = commentMapper.selectByMap(map);
        return comments;
    }


    @Override
    @CacheEvict(value = {COMMENTS_CACHE_NAME}, allEntries = true, beforeInvocation = true)
    @Transactional(rollbackFor = Exception.class)
    public Comment updateCommentStatus(Long commentId, Integer status) {
        //子评论随父评论状态一起改变
        //1.修改该评论状态
        Comment comment = findCommentById(commentId);
        comment.setCommentStatus(status);
        commentMapper.updateById(comment);
        //2.修改该评论的子评论状态
        List<Long> childIds = commentMapper.selectChildCommentIds(comment.getPathTrace() + commentId + "/");
        childIds.forEach(id -> commentMapper.updateCommentStatus(id, status));
        //3.修改文章评论数
        postService.updateCommentSize(comment.getPostId());
        return comment;
    }


    @Override
    @Cacheable(value = COMMENTS_CACHE_NAME, key = "'comments_id_'+#commentId", unless = "#result == null")
    public Comment findCommentById(Long commentId) {
        return commentMapper.selectById(commentId);
    }


    @Override
    public Page<Comment> pagingCommentsByPostAndCommentStatus(Long postId, Integer status, Page<Comment> page) {
        return page.setRecords(commentMapper.pagingByPostIdAndCommentStatus(postId, status, page));
    }

    @Override
    @Cacheable(value = COMMENTS_CACHE_NAME, key = "'comments_postid_'+#postId+'_status_'+#status")
    public List<Comment> findCommentsByPostAndCommentStatus(Long postId, Integer status) {
        return commentMapper.findByPostIdAndCommentStatus(postId, status);
    }

    @Override
    @Cacheable(value = COMMENTS_CACHE_NAME, key = "'comments_postid_'+#postId+'_notstatus_'+#status")
    public List<Comment> findCommentsByPostAndCommentStatusNot(Long postId, Integer status) {
        return commentMapper.findByPostIdAndCommentStatusNot(postId, status);
    }

    @Override
    @Cacheable(value = COMMENTS_CACHE_NAME, key = "'comments_latest_'+#limit")
    public List<Comment> findCommentsLatest(Integer limit) {
        List<Comment> comments;
        if (StringUtils.equals(SensConst.OPTIONS.get(BlogPropertiesEnum.NEW_COMMENT_NEED_CHECK.getProp()), TrueFalseEnum.TRUE.getDesc()) || SensConst.OPTIONS.get(BlogPropertiesEnum.NEW_COMMENT_NEED_CHECK.getProp()) == null) {
            comments = commentMapper.findTopTenByStatus(CommentStatusEnum.PUBLISHED.getCode(), limit);
        } else {
            comments = commentMapper.findTopTenByStatusNot(CommentStatusEnum.RECYCLE.getCode(), limit);
        }
        //处理一下，去掉格式
        comments.forEach(comment -> comment.setCommentContent(HtmlUtil.cleanHtmlTag(comment.getCommentContent())));
        return comments;
    }


    @Override
    @Cacheable(value = COMMENTS_CACHE_NAME, key = "'comments_count_status_'+#status")
    public Integer countByStatus(Integer status) {
        return commentMapper.countByStatus(status);
    }

    @Override
    @Cacheable(value = COMMENTS_CACHE_NAME, key = "'comments_recent_50'")
    public List<Comment> findCommentsTop50() {
        return commentMapper.findCommentByLimit(50);
    }

    @Override
    @Cacheable(value = COMMENTS_CACHE_NAME, key = "'comments_count_uid_'+#userId")
    public Integer countByUserId(Long userId) {
        return commentMapper.countByUserId(userId);
    }

    @Override
    public Integer countByReceiveUserAndStatus(Long userId, Integer status) {
        //获得该用户的所有文章Id
        List<Long> postIds = postService.selectIdsByUserId(userId);
        //然后求文章内的所有评论
        Integer size = 0;
        if (postIds != null && postIds.size() > 0) {
            size = commentMapper.countByPostIds(postIds, status);
        }
        return size;
    }

    @Override
    public Comment getLastestComment(String ip) {
        return commentMapper.getLastestComment(ip);
    }

}
