package com.github.mybatis.filer.segment;

import cn.org.atool.fluent.mybatis.If;
import cn.org.atool.fluent.mybatis.base.crud.BaseUpdate;
import cn.org.atool.fluent.mybatis.base.model.FieldMapping;
import cn.org.atool.fluent.mybatis.metadata.DbType;
import com.github.mybatis.base.FluentClassName;
import com.github.mybatis.entity.FluentEntity;
import com.github.mybatis.filer.AbstractFiler;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.Map;

import static cn.org.atool.fluent.mybatis.mapper.FluentConst.Pack_Wrapper;
import static cn.org.atool.fluent.mybatis.mapper.FluentConst.Suffix_Update;
import static com.github.mybatis.base.MethodName.*;
import static com.github.mybatis.base.MethodName.M_COLUMN2MAPPING;
import static com.github.mybatis.base.MethodName.M_DEFAULT_UPDATER;
import static com.github.mybatis.base.MethodName.M_NEW_UPDATER;
import static com.github.mybatis.filer.ClassNames2.CN_List_Str;
import static com.github.mybatis.filer.ClassNames2.CN_Supplier_Str;

/**
 * updater代码生成
 *
 * @author wudarui
 */
public class UpdaterFiler extends AbstractFiler {
    public UpdaterFiler(FluentEntity fluentEntity) {
        super(fluentEntity);
        this.packageName = getPackageName(fluentEntity);
        this.klassName = getClassName(fluentEntity);
        this.comment = "更新构造";
    }

    public static String getClassName(FluentClassName fluentEntity) {
        return fluentEntity.getNoSuffix() + Suffix_Update;
    }

    public static String getPackageName(FluentClassName fluentEntity) {
        return fluentEntity.getPackageName(Pack_Wrapper);
    }

    @Override
    protected void staticImport(JavaFile.Builder spec) {
        spec.addStaticImport(If.class, "notBlank");
    }

    @Override
    protected void build(TypeSpec.Builder builder) {
        builder.superclass(this.superKlass())
            .addField(this.f_defaults())
            .addField(this.f_setter())
            .addField(this.f_update())
            .addField(this.f_where())
            .addField(this.f_orderBy())
            .addMethod(this.constructor0())
            .addMethod(this.constructor2_supplier_string())
            .addMethod(this.m_where())
            .addMethod(this.m_mapping())
            .addMethod(this.m_allFields())
            .addMethod(this.m_dbType())
            .addMethod(this.m_emptyUpdater())
            .addMethod(this.m_emptyUpdater_table())
            .addMethod(this.m_defaultUpdater())
            .addMethod(this.m_column2mapping())
        ;
    }

    /**
     * public final UpdateSetter update = new UpdateSetter(this);
     *
     * @return FieldSpec
     */
    private FieldSpec f_update() {
        return FieldSpec.builder(fluent.updateSetter(),
            "update", Modifier.PUBLIC, Modifier.FINAL)
            .initializer("set")
            .addJavadoc("replaced by {@link #set}")
            .addAnnotation(Deprecated.class)
            .build();
    }

    private FieldSpec f_setter() {
        return FieldSpec.builder(fluent.updateSetter(),
            "set", Modifier.PUBLIC, Modifier.FINAL)
            .initializer("new UpdateSetter(this)")
            .addJavadoc("same as {@link #update}")
            .build();
    }

    /**
     * public final UpdateWhere where = new UpdateWhere(this);
     *
     * @return FieldSpec
     */
    private FieldSpec f_where() {
        return FieldSpec.builder(fluent.updateWhere(),
            "where", Modifier.PUBLIC, Modifier.FINAL)
            .initializer("new UpdateWhere(this)")
            .build();
    }

    /**
     * public final UpdateOrderBy orderBy = new UpdateOrderBy(this);
     *
     * @return FieldSpec
     */
    private FieldSpec f_orderBy() {
        return FieldSpec.builder(fluent.updateOrderBy(),
            "orderBy", Modifier.PUBLIC, Modifier.FINAL)
            .initializer("new UpdateOrderBy(this)")
            .build();
    }

    /**
     * public EntityUpdate() {}
     *
     * @return FieldSpec
     */
    private MethodSpec constructor0() {
        return MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addStatement("this(defaults.table(), null)")
            .build();
    }

    private MethodSpec constructor2_supplier_string() {
        return MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(CN_Supplier_Str, "table")
            .addParameter(String.class, "alias")
            .addStatement("super(table, alias, $T.class, $T.class)", fluent.entity(), fluent.query())
            .build();
    }

    private ParameterizedTypeName superKlass() {
        ClassName base = ClassName.get(BaseUpdate.class);
        ClassName entity = fluent.entity();
        ClassName updater = fluent.updater();
        ClassName query = fluent.query();
        return ParameterizedTypeName.get(base, entity, updater, query);
    }

    /**
     * public UpdateWhere where() {}
     *
     * @return FieldSpec
     */
    private MethodSpec m_where() {
        return super.publicMethod("where", true, fluent.updateWhere())
            .addStatement("return this.where")
            .build();
    }

    private MethodSpec m_emptyUpdater() {
        return super.publicMethod(M_NEW_UPDATER, false, fluent.updater())
            .addModifiers(Modifier.STATIC)
            .addStatement("return new $T()", fluent.updater())
            .build();
    }

    private MethodSpec m_emptyUpdater_table() {
        return super.publicMethod(M_NEW_UPDATER, false, fluent.updater())
            .addModifiers(Modifier.STATIC)
            .addParameter(CN_Supplier_Str, "table")
            .addStatement("return new $T(table, null)", fluent.updater())
            .build();
    }

    private MethodSpec m_defaultUpdater() {
        return super.publicMethod(M_DEFAULT_UPDATER, false, fluent.updater())
            .addModifiers(Modifier.STATIC)
            .addStatement("return defaults.defaultUpdater()")
            .build();
    }

    private MethodSpec m_column2mapping() {
        return super.protectedMethod(M_COLUMN2MAPPING, true, ParameterizedTypeName.get(Map.class, String.class, FieldMapping.class))
            .addStatement("return $T.Column2Mapping", fluent.mapping())
            .build();
    }

    private MethodSpec m_allFields() {
        return MethodSpec.methodBuilder("allFields")
            .addModifiers(Modifier.PROTECTED)
            .returns(CN_List_Str)
            .addStatement("return $T.ALL_COLUMNS", fluent.mapping())
            .build();
    }

    private MethodSpec m_dbType() {
        return super.publicMethod("dbType", true, DbType.class)
            .addStatement("return $T.$L", DbType.class, fluent.getDbType().name())
            .build();
    }

    @Override
    protected boolean isInterface() {
        return false;
    }
}