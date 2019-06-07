package com.liuyanzhao.sens.service;

import com.liuyanzhao.sens.entity.MailRetrieve;

/**
 * @author 言曌
 * @date 2019/1/30 上午11:20
 */

public interface MailRetrieveService {

    /**
     * 新增/修改友情邮件记录
     *
     * @param mailRetrieve mailRetrieve
     * @return MailRetrieve
     */
    MailRetrieve saveByMailRetrieve(MailRetrieve mailRetrieve);

    /**
     * 根据编号删除
     *
     * @param mailRetrieveId mailRetrieveId
     * @return MailRetrieve
     */
    void removeByMailRetrieveId(Long mailRetrieveId);


    /**
     * 根据编号查询单个邮件记录
     *
     * @param id id
     * @return MailRetrieve 记录
     */
    MailRetrieve findById(Long id);

    /**
     * 根据用户Id查询单个邮件记录
     *
     * @param userId 用户Id
     * @return 记录
     */
    MailRetrieve findLatestByUserId(Long userId);

    /**
     * 根据邮箱查询单个邮件记录
     *
     * @param email 邮箱
     * @return 记录
     */
    MailRetrieve findLatestByEmail(String email);

}
