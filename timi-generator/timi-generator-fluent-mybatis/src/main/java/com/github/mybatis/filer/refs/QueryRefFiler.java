package com.github.mybatis.filer.refs;

import cn.org.atool.fluent.mybatis.base.crud.IDefaultGetter;
import cn.org.atool.fluent.mybatis.base.crud.IQuery;
import cn.org.atool.fluent.mybatis.base.crud.IUpdate;
import cn.org.atool.fluent.mybatis.utility.MybatisUtil;
import com.github.mybatis.javafile.AbstractFile;
import com.github.mybatis.entity.FluentEntity;
import com.github.mybatis.entity.FluentList;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.mybatis.base.MethodName.M_NOT_FLUENT_MYBATIS_EXCEPTION;

public class QueryRefFiler extends AbstractFile {
    private static final String QueryRef = "QueryRef";

    public static ClassName getClassName() {
        return ClassName.get(FluentList.refsPackage(), QueryRef);
    }

    public QueryRefFiler() {
        this.packageName = FluentList.refsPackage();
        this.klassName = QueryRef;
        this.comment = "构造Entity对应的default query\n更新器工厂类单例引用";
    }

    @Override
    protected void staticImport(JavaFile.Builder builder) {
        builder.addStaticImport(MybatisUtil.class, M_NOT_FLUENT_MYBATIS_EXCEPTION);
        super.staticImport(builder);
    }

    @Override
    protected void build(TypeSpec.Builder spec) {
        for (FluentEntity fluent : FluentList.getFluents()) {
            spec.addField(this.f_factory(fluent));
        }
        spec.addField(this.f_allQuerySupplier())
            .addStaticBlock(this.m_initSupplier())
            .addMethod(m_defaultQuery(false))
            .addMethod(m_emptyQuery(false))
            .addMethod(m_defaultUpdater(false))
            .addMethod(m_emptyUpdater(false))
            .addMethod(this.m_findDefaultGetter());
    }

    private FieldSpec f_factory(FluentEntity fluent) {
        return FieldSpec.builder(fluent.defaults(), fluent.lowerNoSuffix(),
            Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .initializer("$T.INSTANCE", fluent.defaults())
            .build();
    }

    public static MethodSpec m_defaultQuery(boolean isRef) {
        MethodSpec.Builder spec = MethodSpec.methodBuilder("defaultQuery")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addParameter(Class.class, "clazz")
            .returns(IQuery.class);
        if (isRef) {
            spec.addAnnotation(Override.class)
                .addStatement("Class entityClass = this.findFluentEntityClass(clazz)")
                .addStatement("return $T.defaultQuery(entityClass)", getClassName());
        } else {
            spec.addModifiers(Modifier.STATIC)
                .addJavadoc("返回clazz实体对应的默认Query实例")
                .addStatement("\treturn findDefault(clazz).defaultQuery()");
        }
        return spec.build();
    }

    public static MethodSpec m_emptyQuery(boolean isRef) {
        MethodSpec.Builder spec = MethodSpec.methodBuilder("emptyQuery")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addParameter(Class.class, "clazz")
            .returns(IQuery.class);
        if (isRef) {
            spec.addAnnotation(Override.class)
                .addStatement("Class entityClass = this.findFluentEntityClass(clazz)")
                .addStatement("return $T.emptyQuery(entityClass)", getClassName());
        } else {
            spec.addModifiers(Modifier.STATIC)
                .addJavadoc("返回clazz实体对应的空Query实例")
                .addStatement("\treturn findDefault(clazz).query()");
        }
        return spec.build();
    }

    public static MethodSpec m_defaultUpdater(boolean isRef) {
        MethodSpec.Builder spec = MethodSpec.methodBuilder("defaultUpdater")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addParameter(Class.class, "clazz")
            .returns(IUpdate.class);
        if (isRef) {
            spec.addAnnotation(Override.class)
                .addStatement("Class entityClass = this.findFluentEntityClass(clazz)")
                .addStatement("return $T.defaultUpdater(entityClass)", getClassName());
        } else {
            spec.addModifiers(Modifier.STATIC)
                .addJavadoc("返回clazz实体对应的默认Updater实例")
                .addStatement("\treturn findDefault(clazz).defaultUpdater()");
        }
        return spec.build();
    }

    public static MethodSpec m_emptyUpdater(boolean isRef) {
        MethodSpec.Builder spec = MethodSpec.methodBuilder("emptyUpdater")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addParameter(Class.class, "clazz")
            .returns(IUpdate.class);
        if (isRef) {
            spec.addAnnotation(Override.class)
                .addStatement("Class entityClass = this.findFluentEntityClass(clazz)")
                .addStatement("return $T.emptyUpdater(entityClass)", getClassName());
        } else {
            spec.addModifiers(Modifier.STATIC)
                .addJavadoc("返回clazz实体对应的空Updater实例")
                .addStatement("\treturn findDefault(clazz).updater()");
        }
        return spec.build();
    }

    private MethodSpec m_findDefaultGetter() {
        MethodSpec.Builder spec = MethodSpec.methodBuilder("findDefault")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addParameter(Class.class, "clazz")
            .returns(IDefaultGetter.class);

        spec.addModifiers(Modifier.STATIC)
            .addCode("if (Supplier.containsKey(clazz)) {\n")
            .addStatement("\treturn Supplier.get(clazz)")
            .addCode("}\n")
            .addStatement("throw $L(clazz)", M_NOT_FLUENT_MYBATIS_EXCEPTION);

        return spec.build();
    }

    private CodeBlock m_initSupplier() {
        List<CodeBlock> list = new ArrayList<>();
        for (FluentEntity fluent : FluentList.getFluents()) {
            list.add(CodeBlock.of("Supplier.put($T.class, $L);\n", fluent.entity(), fluent.lowerNoSuffix()));
        }
        return CodeBlock.join(list, "");
    }

    private FieldSpec f_allQuerySupplier() {
        return FieldSpec.builder(parameterizedType(Map.class, Class.class, IDefaultGetter.class), "Supplier",
            Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
            .initializer("new $T<>()", HashMap.class)
            .build();
    }

    @Override
    protected boolean isInterface() {
        return false;
    }

    protected String generatorName() {
        return "FluentMybatis";
    }
}