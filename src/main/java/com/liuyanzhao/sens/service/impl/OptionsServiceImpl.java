package com.liuyanzhao.sens.service.impl;

import com.liuyanzhao.sens.entity.Options;
import com.liuyanzhao.sens.mapper.OptionsMapper;
import com.liuyanzhao.sens.service.OptionsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *     系统设置业务逻辑实现类
 * </pre>
 *
 * @author : saysky
 * @date : 2017/11/14
 */
@Service
public class OptionsServiceImpl implements OptionsService {

    private static final String OPTIONS_CACHE_NAME = "options";


    @Autowired(required = false)
    private OptionsMapper optionsMapper;

    /**
     * 批量保存设置
     *
     * @param options options
     */
    @Override
    @CacheEvict(value = OPTIONS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public void saveOptions(Map<String, String> options) {
        if (null != options && !options.isEmpty()) {
            options.forEach((k, v) -> saveOption(k, v));
        }
    }

    /**
     * 保存单个设置选项
     *
     * @param key   key
     * @param value value
     */
    @Override
    @CacheEvict(value = OPTIONS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public void saveOption(String key, String value) {
        Options options = null;
        if (StringUtils.equals(value, "")) {
            options = new Options();
            options.setOptionName(key);
            this.removeOption(options);
        } else {
            if (StringUtils.isNotEmpty(key)) {
                //如果查询到有该设置选项则做更新操作，反之保存新的设置选项
                if (null == optionsMapper.findOptionsByOptionName(key)) {
                    options = new Options();
                    options.setOptionName(key);
                    options.setOptionValue(value);
                    optionsMapper.insert(options);
                } else {
                    options = optionsMapper.findOptionsByOptionName(key);
                    options.setOptionValue(value);
                    optionsMapper.updateById(options);
                }
            }
        }
    }

    /**
     * 移除设置项
     *
     * @param options options
     */
    @Override
    @CacheEvict(value = OPTIONS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public void removeOption(Options options) {
        optionsMapper.deleteById(options);
    }

    /**
     * 获取设置选项
     *
     * @return Map
     */
    @Override
    @Cacheable(value = OPTIONS_CACHE_NAME, key = "'options_all'")
    public Map<String, String> findAllOptions() {
        Map<String, String> options = new HashMap<>();
        List<Options> optionsList = optionsMapper.findAll();
        if (null != optionsList) {
            optionsList.forEach(option -> options.put(option.getOptionName(), option.getOptionValue()));
        }
        return options;
    }

    /**
     * 根据key查询单个设置选项
     *
     * @param key key
     * @return String
     */
    @Override
    @Cacheable(value = OPTIONS_CACHE_NAME, key = "'options_key_'+#key", unless = "#result == null")
    public String findOneOption(String key) {
        Options options = optionsMapper.findOptionsByOptionName(key);
        if (null != options) {
            return options.getOptionValue();
        }
        return null;
    }
}
