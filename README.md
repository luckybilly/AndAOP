android 中进行AOP编程
===

简介
---

AndAOP是一个提供在android中进行AOP编程的gradle插件。

使用此插件可以通过编写正常书写java代码类来实现AOP逻辑。

不需要了解字节码即可实现字节码插桩。

module介绍：
- aop： AndAOP插件源码module
- app： AndAOP插件测试（主程序）
- lib_service: AndAOP插件测试（lib module程序）

使用方式
---
基本用法可以参考app/src/main/java/com/billy/andaop/Aop类来实现
高级用法可以在此基础上实现更多功能

- 在需要aop的app module的build.gradle中添加buildscript，并apply aop插件
- 配置aop信息

    <pre>
    aopClass: 用来处理aop逻辑的类
    methodStart: aopClass中的静态方法，通过asm将aopClass.methodStart(str, str, str)插入到方法的开始
        methodStart方法必须接收3个参数：类名，方法名，方法参数列表及返回值类型
        methodStart方法必须返回aopClass对象
    methodEnd: aopClass中的静态方法，通过asm将aopClass.methodEnd(aop)插入到方法的所有return和throw语句之前
    include: 正则表达式，符合条件的类会被asm注入aop代码，（类的包分隔符使用'/'而不是'.'）
    exclude: 同include，符合条件的类会从include中排除 (默认排除了BuildConfig,R及R$...)
    </pre>
	
 例子：
        
    buildscript {
        repositories {
            jcenter()
        }
        dependencies {
            classpath 'AndAOP:aop:1.0.1'
        }
    }
    apply plugin: 'and-aop'
    
    andAop {
        aopClass = 'com.billy.andaop.Aop'
        methodStart = 'beforeStart'
        methodEnd = 'afterEnd'
        include = [//正则表达式
                'com/billy/andaop/.*'   
                , 'com/billy/lib/.*'
        ]
        exclude = [//正则表达式
                'com/billy/andaop/TimeWatcher'
        ]
    }



实现原理
---

从Gradle1.5开始，包含了Transform API，
允许第三方插件在class文件转为为dex文件前操作编译好的class文件
添加一个transform的方式：

    android.registerTransform(theTransform) 
    //or 
    android.registerTransform(theTransform, dependencies)


- 为了便于复用，我们先创建一个插件项目(aop)
- 在此项目中定义插件aop(AndAopPlugin.groovy & resources/META-INF/gradle-plugins/and-aop.properties)，
- 在插件中注册一个自定义的Transform (AndAopTransform.groovy)
- 在 AndAopTransform.transform中遍历所有依赖的jar（包括直接依赖的jar包、aar中的jar、lib module编译后的jar）
- 在 AndAopTransform.transform中遍历所有当前module编译后的class
- 根据过滤规则，使用ASM对jar中的class和当前module的class文件进行字节码插桩

        主要实现逻辑在AndAopProcessor.groovy类中
    
- 修改后的class作为 dex transform 的input
- 于是就完成了字节码层面的AOP

适用范围
---
可以对所有打包到dex文件中的java代码进行AOP（不包括C/C++代码）

- 当前app module中的代码
- 当前project中其它lib module中的代码
- 通过 compile fileTree(dir: 'libs', include: ['*.jar']) 依赖的jar包中的代码
- 通过maven方式依赖的其它aar包中的代码(其本质也是读取aar中的jar包中的class)



参考资料
---

- [如何使用Android Studio开发Gradle插件](http://blog.csdn.net/sbsujjbcy/article/details/50782830)        
- [Android 热修复使用Gradle Plugin1.5改造Nuwa插件](http://blog.csdn.net/sbsujjbcy/article/details/50839263)
- [Android 热修复方案Tinker(七) 插桩实现](http://blog.csdn.net/l2show/article/details/54846682)
- [Android 热修复Nuwa的原理及Gradle插件源码解析](http://blog.csdn.net/sbsujjbcy/article/details/50812674)
- [Transform API](http://tools.android.com/tech-docs/new-build-system/transform-api)
- [ASM英文使用说明](http://asm.ow2.org/current/asm-transformations.pdf)





