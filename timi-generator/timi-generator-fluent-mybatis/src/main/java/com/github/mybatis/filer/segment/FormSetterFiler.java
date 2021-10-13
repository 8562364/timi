package com.github.mybatis.filer.segment;

import cn.org.atool.fluent.mybatis.base.crud.FormSetter;
import cn.org.atool.fluent.mybatis.functions.FormApply;
import cn.org.atool.fluent.mybatis.model.Form;
import cn.org.atool.fluent.mybatis.model.IFormApply;
import com.github.mybatis.base.FluentClassName;
import com.github.mybatis.entity.FluentEntity;
import com.github.mybatis.filer.AbstractFiler;
import cn.org.atool.fluent.mybatis.utility.MybatisUtil;
import cn.org.atool.fluent.mybatis.utility.PoJoHelper;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.Map;
import java.util.function.Function;

import static cn.org.atool.fluent.mybatis.mapper.FluentConst.Pack_Helper;
import static cn.org.atool.fluent.mybatis.mapper.FluentConst.Suffix_EntityFormSetter;

public class FormSetterFiler extends AbstractFiler {

    public static String getClassName(FluentClassName fluentEntity) {
        return fluentEntity.getNoSuffix() + Suffix_EntityFormSetter;
    }

    public static String getPackageName(FluentClassName fluentEntity) {
        return fluentEntity.getPackageName(Pack_Helper);
    }

    @Override
    protected void staticImport(JavaFile.Builder spec) {
        spec.addStaticImport(MybatisUtil.class, "assertNotNull");
        super.staticImport(spec);
    }

    public FormSetterFiler(FluentEntity fluent) {
        super(fluent);
        this.packageName = getPackageName(fluent);
        this.klassName = getClassName(fluent);
        this.comment = "Form Column Setter";
    }

    @Override
    protected void build(TypeSpec.Builder builder) {
        TypeName applyName = parameterizedType(ClassName.get(IFormApply.class), fluent.entity(), fluent.formSetter());
        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .superclass(FormSetter.class)
            .addSuperinterface(super.parameterizedType(fluent.segment(), applyName))
            .addMethod(this.constructor1())
            .addMethod(this.m_entityClass())
            .addMethod(this.m_byObject())
        ;
    }

    private MethodSpec constructor1() {
        return MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PRIVATE)
            .addParameter(FormApply.class, "apply")
            .addStatement("super.formApply = apply")
            .build();
    }

    private MethodSpec m_entityClass() {
        return super.publicMethod("entityClass", true, Class.class)
            .addStatement("return $T.class", fluent.entity())
            .build();
    }

    private MethodSpec m_byObject() {
        return super.publicMethod("by", false,
            parameterizedType(ClassName.get(IFormApply.class), fluent.entity(), fluent.formSetter()))
            .addModifiers(Modifier.STATIC)
            .addParameter(Object.class, "object")
            .addParameter(Form.class, "form")
            .addStatement("assertNotNull($S, object)", "object")
            .addStatement("$T map = $T.toMap(object)", Map.class, PoJoHelper.class)
            .addStatement("$T<FormApply, FormSetter> apply = $T::new", Function.class, fluent.formSetter())
            .addStatement("return new $T<>(apply, map, form)", FormApply.class)
            .build();
    }

    @Override
    protected boolean isInterface() {
        return false;
    }
}