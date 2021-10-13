package com.github.mybatis.filer.refs;

import cn.org.atool.fluent.mybatis.base.IRefs;
import cn.org.atool.fluent.mybatis.base.mapper.IRichMapper;
import cn.org.atool.fluent.mybatis.metadata.DbType;
import com.github.mybatis.javafile.AbstractFile;
import com.github.mybatis.entity.FluentList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;

import static cn.org.atool.fluent.mybatis.If.isBlank;
import static com.github.mybatis.filer.ClassNames2.CN_Class_IEntity;
import static com.github.mybatis.filer.ClassNames2.CN_Set;
import static com.github.mybatis.filer.refs.FieldRefFiler.m_findColumnByField;
import static com.github.mybatis.filer.refs.FieldRefFiler.m_findPrimaryColumn;
import static com.github.mybatis.filer.refs.QueryRefFiler.m_defaultQuery;
import static com.github.mybatis.filer.refs.QueryRefFiler.m_defaultUpdater;
import static com.github.mybatis.filer.refs.QueryRefFiler.m_emptyQuery;
import static com.github.mybatis.filer.refs.QueryRefFiler.m_emptyUpdater;

/**
 * AllRef 文件构造
 *
 * @author darui.wu
 */
public class AllRefFiler extends AbstractFile {
    private static final String AllRef = "AllRef";

    public static ClassName getClassName() {
        return ClassName.get(FluentList.refsPackage(), AllRef);
    }

    public AllRefFiler() {
        this.packageName = FluentList.refsPackage();
        this.klassName = AllRef;
        this.comment = "应用所有Mapper Bean引用";
    }

    @Override
    protected void build(TypeSpec.Builder spec) {
        spec.superclass(IRefs.class)
            .addModifiers(Modifier.ABSTRACT);
        spec.modifiers.remove(Modifier.PUBLIC);

        spec.addField(f_mappers())
            .addMethod(this.m_constructor())
            .addMethod(this.m_mappers())
            .addMethod(this.m_getMapper())
            .addMethod(m_findColumnByField(true))
            .addMethod(m_findPrimaryColumn(true))
            .addMethod(m_defaultQuery(true))
            .addMethod(m_emptyQuery(true))
            .addMethod(m_defaultUpdater(true))
            .addMethod(m_emptyUpdater(true))
            .addMethod(this.m_allEntityClass())
            .addMethod(this.m_initEntityMapper());
        spec.addType(this.class_field())
            .addType(this.class_query())
            .addType(this.class_setter());
    }

    private FieldSpec f_mappers() {
        return FieldSpec.builder(MapperRefFiler.getClassName(), "mappers",
            Modifier.PRIVATE, Modifier.STATIC).build();
    }

    private MethodSpec m_constructor() {
        MethodSpec.Builder spec = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC);
        if (isBlank(FluentList.getDbType())) {
            spec.addStatement("super.setDefaultDbType(null)");
        } else {
            spec.addStatement("super.setDefaultDbType($T.$L)", DbType.class, FluentList.getDbType());
        }
        return spec.build();
    }

    private MethodSpec m_allEntityClass() {
        return MethodSpec.methodBuilder("allEntityClass")
            .addModifiers(Modifier.PROTECTED, Modifier.FINAL)
            .addAnnotation(Override.class)
            .returns(parameterizedType(CN_Set, CN_Class_IEntity))
            .addStatement("return $T.allEntityClass()", MapperRefFiler.getClassName())
            .build();
    }

    private MethodSpec m_mappers() {
        return MethodSpec.methodBuilder("mapper")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(MapperRefFiler.getClassName())
            .beginControlFlow("if (mappers == null)")
            .addStatement("throw springNotInitException()")
            .endControlFlow()
            .addStatement("return mappers")
            .build();
    }

    private MethodSpec m_getMapper() {
        return MethodSpec.methodBuilder("getMapper")
            .addModifiers(Modifier.PROTECTED, Modifier.FINAL)
            .addParameter(CN_Class_IEntity, "clazz")
            .returns(IRichMapper.class)
            .addStatement("Class<? extends IEntity> entityClass = super.findFluentEntityClass(clazz)")
            .addStatement("return MapperRef.mapper(entityClass)")
            .build();
    }

    private MethodSpec m_initEntityMapper() {
        MethodSpec.Builder spec = MethodSpec.methodBuilder("initEntityMapper")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.FINAL, Modifier.PROTECTED);
        spec.addStatement("mappers = $T.instance(super.mapperFactory)", MapperRefFiler.getClassName());
        return spec.build();
    }

    private TypeSpec class_field() {
        return TypeSpec.classBuilder("Field")
            .addModifiers(Modifier.STATIC, Modifier.PUBLIC, Modifier.FINAL)
            .superclass(FieldRefFiler.getClassName())
            .build();
    }

    private TypeSpec class_query() {
        return TypeSpec.classBuilder("Query")
            .addModifiers(Modifier.STATIC, Modifier.PUBLIC, Modifier.FINAL)
            .superclass(QueryRefFiler.getClassName())
            .build();
    }

    private TypeSpec class_setter() {
        return TypeSpec.classBuilder("Form")
            .addModifiers(Modifier.STATIC, Modifier.PUBLIC, Modifier.FINAL)
            .addSuperinterface(FormRefFiler.getClassName())
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