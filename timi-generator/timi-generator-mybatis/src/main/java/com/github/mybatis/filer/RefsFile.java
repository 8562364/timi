package com.github.mybatis.filer;

import cn.org.atool.fluent.mybatis.base.IRefs;
import com.github.mybatis.javafile.AbstractFile;
import com.github.mybatis.entity.EntityRefMethod;
import com.github.mybatis.entity.FluentEntity;
import com.github.mybatis.entity.FluentList;
import com.github.mybatis.filer.refs.AllRefFiler;
import com.github.mybatis.filer.refs.EntityRelationFiler;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.Map;

import static cn.org.atool.fluent.mybatis.utility.MybatisUtil.capitalFirst;

/**
 * Mappers 代码生成
 *
 * @author darui.wu
 */
public class RefsFile extends AbstractFile {

    private static final String Refs = "Refs";

    public static ClassName getClassName() {
        return ClassName.get(FluentList.refsPackage(), Refs);
    }

    public RefsFile() {
        this.packageName = FluentList.refsPackage();
        this.klassName = Refs;
        this.comment = "" +
            "\n o - 查询器，更新器工厂类单例引用" +
            "\n o - 应用所有Mapper Bean引用" +
            "\n o - Entity关联对象延迟加载查询实现";
    }

    @Override
    protected void build(TypeSpec.Builder spec) {
        spec.superclass(AllRefFiler.getClassName())
            .addModifiers(Modifier.FINAL)
            .addMethod(this.m_instance());
        for (FluentEntity fluent : FluentList.getFluents()) {
            for (EntityRefMethod refMethod : fluent.getRefMethods()) {
                if (refMethod.isAbstractMethod()) {
                    spec.addMethod(this.m_refMethod(fluent, refMethod));
                } else {
                    spec.addMethod(this.m_refRealMethod(fluent, refMethod));
                }
            }
        }
    }

    private MethodSpec m_refMethod(FluentEntity fluent, EntityRefMethod refMethod) {
        String methodName = refMethod.getRefMethod(fluent);
        return MethodSpec.methodBuilder(methodName)
            .addParameter(fluent.entity(), "entity")
            .addModifiers(Modifier.PUBLIC)
            .returns(refMethod.getJavaType())
            .addJavadoc("{@link $L#$L}", fluent.getClassName(), refMethod.getName())
            .addCode("if (relation instanceof $T) {\n", EntityRelationFiler.getClassName())
            .addStatement("\treturn (($T)relation).$L(entity)", EntityRelationFiler.getClassName(), methodName)
            .addCode("} else {\n")
            .addStatement("\tthrow new $T($S)", RuntimeException.class, "It must implement IEntityRelation and add the implementation to spring management.")
            .addCode("}")
            .build();
    }

    private MethodSpec m_refRealMethod(FluentEntity fluent, EntityRefMethod refMethod) {
        FluentEntity ref = FluentList.getFluentEntity(refMethod.getReturnEntity());

        MethodSpec.Builder spec = MethodSpec.methodBuilder(refMethod.getRefMethod(fluent))
            .addParameter(fluent.entity(), "entity")
            .addModifiers(Modifier.PUBLIC)
            .returns(refMethod.getJavaType())
            .addJavadoc("{@link $L#$L}", fluent.getClassName(), refMethod.getName());
        String method = refMethod.returnList() ? "listEntity" : "findOne";
        spec.addCode("return mapper().$LMapper.$L(new $T()\n", ref.lowerNoSuffix(), method, ref.query());
        int index = 0;
        for (Map.Entry<String, String> pair : refMethod.getMapping().entrySet()) {
            spec.addCode(index == 0 ? "\t.where" : "\t.and")
                .addCode(".$L().eq(entity.get$L())\n", pair.getKey(), capitalFirst(pair.getValue(), ""));
            index++;
        }
        spec.addStatement("\t.end())");
        return spec.build();
    }

    private MethodSpec m_instance() {
        return MethodSpec.methodBuilder("instance")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .addJavadoc("Refs 单例")
            .returns(RefsFile.getClassName())
            .addStatement("return ($L) $T.instance()", Refs, IRefs.class)
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