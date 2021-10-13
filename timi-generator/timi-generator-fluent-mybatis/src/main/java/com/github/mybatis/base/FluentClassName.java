package com.github.mybatis.base;

import cn.org.atool.fluent.mybatis.utility.MybatisUtil;
import com.github.mybatis.entity.CommonField;
import com.github.mybatis.filer.segment.DefaultsFiler;
import com.github.mybatis.filer.segment.EntityHelperFiler;
import com.github.mybatis.filer.segment.FormSetterFiler;
import com.github.mybatis.filer.segment.MapperFiler;
import com.github.mybatis.filer.segment.MappingFiler;
import com.github.mybatis.filer.segment.QueryFiler;
import com.github.mybatis.filer.segment.SqlProviderFiler;
import com.github.mybatis.filer.segment.UpdaterFiler;
import com.github.mybatis.filer.segment.WrapperHelperFiler;
import com.squareup.javapoet.ClassName;

import java.util.List;
import java.util.stream.Collectors;

import static cn.org.atool.fluent.mybatis.mapper.FluentConst.Suffix_GroupBy;
import static cn.org.atool.fluent.mybatis.mapper.FluentConst.Suffix_Having;
import static cn.org.atool.fluent.mybatis.mapper.FluentConst.Suffix_ISegment;
import static cn.org.atool.fluent.mybatis.mapper.FluentConst.Suffix_QueryOrderBy;
import static cn.org.atool.fluent.mybatis.mapper.FluentConst.Suffix_QueryWhere;
import static cn.org.atool.fluent.mybatis.mapper.FluentConst.Suffix_Selector;
import static cn.org.atool.fluent.mybatis.mapper.FluentConst.Suffix_UpdateOrderBy;
import static cn.org.atool.fluent.mybatis.mapper.FluentConst.Suffix_UpdateSetter;
import static cn.org.atool.fluent.mybatis.mapper.FluentConst.Suffix_UpdateWhere;

/**
 * fluent entity构造各模块ClassName基类
 *
 * @author darui.wu
 */
@SuppressWarnings("unused")
public abstract class FluentClassName {

    public abstract String getNoSuffix();

    /**
     * 首字母小写,不带Entity后缀的entity名称
     *
     * @return ignore
     */
    public String lowerNoSuffix() {
        return MybatisUtil.lowerFirst(this.getNoSuffix(), "");
    }


    public abstract String getBasePack();

    public abstract String getEntityPack();

    public String getPackageName(String suffix) {
        return this.getBasePack() + "." + suffix;
    }

    public abstract String getClassName();

    public abstract List<CommonField> getFields();

    /**
     * 所有字段拼接在一起
     */
    private String All_Fields = null;

    public String getAllFields() {
        if (this.All_Fields == null) {
            All_Fields = this.getFields().stream().map(CommonField::getColumn).collect(Collectors.joining(", "));
        }
        return All_Fields;
    }

    // all ClassName

    /**
     * ClassName of XyzEntity
     *
     * @return Entity ClassName
     */
    public ClassName entity() {
        return ClassName.get(this.getEntityPack(), this.getClassName());
    }

    /**
     * ClassName of XyzUpdater
     *
     * @return Update ClassName
     */
    public ClassName updater() {
        return ClassName.get(
            UpdaterFiler.getPackageName(this),
            UpdaterFiler.getClassName(this));
    }

    /**
     * ClassName of XyzEntityHelper
     *
     * @return EntityHelper ClassName
     */
    public ClassName entityHelper() {
        return ClassName.get(
            EntityHelperFiler.getPackageName(this),
            EntityHelperFiler.getClassName(this));
    }

    /**
     * ClassName of XyzMapper
     *
     * @return Mapper ClassName
     */
    public ClassName mapper() {
        return ClassName.get(
            MapperFiler.getPackageName(this),
            MapperFiler.getClassName(this));
    }

    /**
     * ClassName of XyzMapping
     *
     * @return Mapping ClassName
     */
    public ClassName mapping() {
        return ClassName.get(
            MappingFiler.getPackageName(this),
            MappingFiler.getClassName(this));
    }

    /**
     * ClassName of XyzQuery
     *
     * @return Query ClassName
     */
    public ClassName query() {
        return ClassName.get(
            QueryFiler.getPackageName(this),
            QueryFiler.getClassName(this));
    }

    /**
     * ClassName of XyzSqlProvider
     *
     * @return SqlProvider ClassName
     */
    public ClassName sqlProvider() {
        return ClassName.get(
            SqlProviderFiler.getPackageName(this),
            SqlProviderFiler.getClassName(this));
    }


    /**
     * ClassName of XyzDefaults
     *
     * @return Defaults ClassName
     */
    public ClassName defaults() {
        return ClassName.get(
            DefaultsFiler.getPackageName(this),
            DefaultsFiler.getClassName(this)
        );
    }

    public ClassName wrapperHelper() {
        return ClassName.get(
            WrapperHelperFiler.getPackageName(this),
            WrapperHelperFiler.getClassName(this));
    }

    public ClassName queryWhere() {
        return ClassName.get(
            WrapperHelperFiler.getPackageName(this)
                + "." +
                WrapperHelperFiler.getClassName(this), Suffix_QueryWhere);
    }

    public ClassName updateWhere() {
        return ClassName.get(
            WrapperHelperFiler.getPackageName(this)
                + "." +
                WrapperHelperFiler.getClassName(this), Suffix_UpdateWhere);
    }

    public ClassName selector() {
        return ClassName.get(
            WrapperHelperFiler.getPackageName(this)
                + "." +
                WrapperHelperFiler.getClassName(this), Suffix_Selector);
    }

    public ClassName groupBy() {
        return ClassName.get(
            WrapperHelperFiler.getPackageName(this)
                + "." +
                WrapperHelperFiler.getClassName(this), Suffix_GroupBy);
    }

    public ClassName having() {
        return ClassName.get(
            WrapperHelperFiler.getPackageName(this)
                + "." +
                WrapperHelperFiler.getClassName(this), Suffix_Having);
    }

    public ClassName queryOrderBy() {
        return ClassName.get(
            WrapperHelperFiler.getPackageName(this)
                + "." +
                WrapperHelperFiler.getClassName(this), Suffix_QueryOrderBy);
    }

    public ClassName updateOrderBy() {
        return ClassName.get(
            WrapperHelperFiler.getPackageName(this)
                + "." +
                WrapperHelperFiler.getClassName(this), Suffix_UpdateOrderBy);
    }

    public ClassName updateSetter() {
        return ClassName.get(
            WrapperHelperFiler.getPackageName(this)
                + "." +
                WrapperHelperFiler.getClassName(this), Suffix_UpdateSetter);
    }

    public ClassName segment() {
        return ClassName.get(
            WrapperHelperFiler.getPackageName(this)
                + "." +
                WrapperHelperFiler.getClassName(this), Suffix_ISegment);
    }

    public ClassName formSetter() {
        return ClassName.get(FormSetterFiler.getPackageName(this), FormSetterFiler.getClassName(this));
    }
}