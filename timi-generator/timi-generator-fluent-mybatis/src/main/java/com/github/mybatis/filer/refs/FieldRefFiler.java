package com.github.mybatis.filer.refs;

import cn.org.atool.fluent.mybatis.base.entity.IMapping;
import com.github.mybatis.entity.FluentEntity;
import com.github.mybatis.entity.FluentList;
import cn.org.atool.fluent.mybatis.utility.MybatisUtil;
import com.github.mybatis.javafile.AbstractFile;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.mybatis.base.MethodName.M_NOT_FLUENT_MYBATIS_EXCEPTION;

/**
 * IMappingRef 文件构造
 *
 * @author darui.wu
 */
public class FieldRefFiler extends AbstractFile {
    private static final String FieldRef = "FieldRef";

    public static ClassName getClassName() {
        return ClassName.get(FluentList.refsPackage(), FieldRef);
    }

    @Override
    protected void staticImport(JavaFile.Builder builder) {
        builder.addStaticImport(MybatisUtil.class, M_NOT_FLUENT_MYBATIS_EXCEPTION);
        super.staticImport(builder);
    }

    public FieldRefFiler() {
        this.packageName = FluentList.refsPackage();
        this.klassName = FieldRef;
        this.comment = "Entity所有Mapping引用";
    }

    @Override
    protected void build(TypeSpec.Builder spec) {
        for (FluentEntity fluent : FluentList.getFluents()) {
            spec.addField(this.class_mapping(fluent));
        }
        spec.addField(this.f_allMappings())
            .addStaticBlock(this.m_initMapping())
            .addMethod(m_findColumnByField(false))
            .addMethod(m_findPrimaryColumn(false));
    }

    private FieldSpec f_allMappings() {
        return FieldSpec.builder(parameterizedType(Map.class, Class.class, IMapping.class),
            "mappings",
            Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
            .initializer("new $T<>()", HashMap.class)
            .build();
    }

    private FieldSpec class_mapping(FluentEntity fluent) {
        return FieldSpec.builder(fluent.mapping(), fluent.getNoSuffix(), Modifier.FINAL, Modifier.PUBLIC, Modifier.STATIC)
            .initializer("$T.MAPPING", fluent.mapping())
            .build();
    }

    private CodeBlock m_initMapping() {
        List<CodeBlock> list = new ArrayList<>();
        for (FluentEntity fluent : FluentList.getFluents()) {
            list.add(CodeBlock.of("mappings.put($T.class, $T.MAPPING);\n", fluent.entity(), fluent.mapping()));
        }
        return CodeBlock.join(list, "");
    }

    public static MethodSpec m_findPrimaryColumn(boolean isRef) {
        MethodSpec.Builder spec = MethodSpec.methodBuilder("findPrimaryColumn")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addParameter(Class.class, "clazz")
            .returns(String.class);
        if (isRef) {
            spec.addAnnotation(Override.class)
                .addStatement("return $T.findPrimaryColumn(clazz)", getClassName());
        } else {
            spec.addModifiers(Modifier.STATIC)
                .addJavadoc("返回clazz属性对应数据库主键字段名称")
                .addCode("if (mappings.containsKey(clazz)) {\n")
                .addStatement("\treturn mappings.get(clazz).findPrimaryColumn()")
                .addCode("}\n")
                .addStatement("throw $L(clazz)", M_NOT_FLUENT_MYBATIS_EXCEPTION);
        }
        return spec.build();
    }

    public static MethodSpec m_findColumnByField(boolean isRef) {
        MethodSpec.Builder spec = MethodSpec.methodBuilder("findColumnByField")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addParameter(Class.class, "clazz")
            .addParameter(String.class, "field")
            .returns(String.class);
        if (isRef) {
            spec.addAnnotation(Override.class)
                .addStatement("return $T.findColumnByField(clazz, field)", getClassName());
        } else {
            spec.addModifiers(Modifier.STATIC)
                .addJavadoc("返回clazz属性field对应的数据库字段名称")
                .addCode("if (mappings.containsKey(clazz)) {\n")
                .addStatement("\treturn mappings.get(clazz).findColumnByField(field)")
                .addCode("}\n")
                .addStatement("throw $L(clazz)", M_NOT_FLUENT_MYBATIS_EXCEPTION);
        }
        return spec.build();
    }

    @Override
    protected boolean isInterface() {
        return false;
    }

    protected String generatorName() {
        return "FluentMybatis";
    }
}