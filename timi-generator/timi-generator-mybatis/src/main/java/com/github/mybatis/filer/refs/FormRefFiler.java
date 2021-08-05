package com.github.mybatis.filer.refs;

import cn.org.atool.fluent.mybatis.functions.FormFunction;
import com.github.mybatis.entity.FluentEntity;
import com.github.mybatis.entity.FluentList;
import cn.org.atool.fluent.mybatis.utility.MybatisUtil;
import com.github.mybatis.javafile.AbstractFile;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;

import static com.github.mybatis.base.MethodName.M_NOT_FLUENT_MYBATIS_EXCEPTION;

/**
 * IMapperRef 文件构造
 *
 * @author darui.wu
 */
public class FormRefFiler extends AbstractFile {
    private static final String FormRef = "FormRef";

    public static ClassName getClassName() {
        return ClassName.get(FluentList.refsPackage(), FormRef);
    }

    public FormRefFiler() {
        this.packageName = FluentList.refsPackage();
        this.klassName = FormRef;
        this.comment = "所有Entity Form Setter引用";
    }

    @Override
    protected void staticImport(JavaFile.Builder builder) {
        builder.addStaticImport(MybatisUtil.class, M_NOT_FLUENT_MYBATIS_EXCEPTION);
        super.staticImport(builder);
    }

    @Override
    protected void build(TypeSpec.Builder spec) {
        for (FluentEntity fluent : FluentList.getFluents()) {
            spec.addField(this.f_formSetter(fluent));
        }
    }

    private FieldSpec f_formSetter(FluentEntity fluent) {
        ClassName cn = fluent.formSetter();
        return FieldSpec.builder(parameterizedType(ClassName.get(FormFunction.class), fluent.entity(), cn)
            , fluent.lowerNoSuffix(), Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
            .initializer("(obj, form) -> $T.by(obj, form)", cn)
            .build();
    }

    @Override
    protected boolean isInterface() {
        return true;
    }

    protected String generatorName() {
        return "FluentMybatis";
    }
}