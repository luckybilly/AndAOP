package com.billy.aop

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin;
import org.gradle.api.Project

import java.util.regex.Pattern

/**
 * @author billy.qi
 * @since 17/3/14 17:35
 */
public class AndAopPlugin implements Plugin<Project> {
    public static final String EXT_NAME = 'andAop'

    @Override
    public void apply(Project project) {
        /**
         * 注册transform接口
         */
        def isApp = project.plugins.hasPlugin(AppPlugin)
        if (isApp) {
            println 'project(' + project.name + ') apply aop plugin'
            project.extensions.create(EXT_NAME, AopExtension)
            def android = project.extensions.getByType(AppExtension)
            def transformImpl = new AndAopTransform(project)
            android.registerTransform(transformImpl)
            project.afterEvaluate {
                init(project)//此处要先于transformImpl.transform方法执行
            }
        }
    }

    void init(Project project) {
        AopExtension extension = project.extensions.findByName(EXT_NAME) as AopExtension
        project.logger.error EXT_NAME + ' configuration is:\n' + extension.toString()
        if (extension == null) extension = new AopExtension(project)
        if (extension.include == null) extension.include = new ArrayList<>()
        if (extension.exclude == null) extension.exclude = new ArrayList<>()
        //将aopClass中的'.'转换为'/'
        if (extension.aopClass)
            extension.aopClass = extension.aopClass.replaceAll('\\.', '/').intern()
        //aopClass添加到排除项
        if (!extension.exclude.contains(extension.aopClass))
            extension.exclude.add(extension.aopClass)
        //添加默认的排除项
        AopExtension.DEFAULT_EXCLUDE.each { exclude ->
            if (!extension.exclude.contains(exclude))
                extension.exclude.add(exclude)
        }
        initPattern(extension.include, extension.includePatterns)
        initPattern(extension.exclude, extension.excludePatterns)
        AndAopProcessor.extension = extension
    }

    private void initPattern(ArrayList<String> list, ArrayList<Pattern> patterns) {
        list.each { s ->
            patterns.add(Pattern.compile(s))
        }
    }
}
