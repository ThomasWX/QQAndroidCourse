package dn.android.architecture.db.framework.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE) // 表示作用范围在类名
@Retention(RetentionPolicy.RUNTIME) // 表示运行时注解有效
public @interface DbTable {
    String value(); // 定义运行传入的值的类型

}
