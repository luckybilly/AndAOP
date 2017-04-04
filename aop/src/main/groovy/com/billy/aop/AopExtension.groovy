package com.billy.aop

import org.gradle.api.Project

import java.util.regex.Pattern
/**
 * aop的配置信息
 * @author billy.qi
 * @since 17/3/28 11:48
 */
class AopExtension {
    static final DEFAULT_EXCLUDE = [
            '.*/R(\\\$)?.*'
            , '.*/BuildConfig$'
    ]
    String aopClass = ''
    String methodStart = ''
    String methodEnd = ''
    ArrayList<String> include = []
    ArrayList<String> exclude = []
    ArrayList<Pattern> includePatterns = []
    ArrayList<Pattern> excludePatterns = []

    public AopExtension(){}
    public AopExtension(Project project) {
    }

    @Override
    String toString() {
        StringBuilder sb = new StringBuilder('andAop:{')
        sb.append('\n\t').append('aopClass').append(':\'').append(aopClass).append('\'')
        sb.append('\n\t').append('methodStart').append(':\'').append(methodStart).append('\'')
        sb.append('\n\t').append('methodEnd').append(':\'').append(methodEnd).append('\'')
        sb.append('\n\t').append('include').append(':').append('[')
        include.each { i ->
            sb.append('\n\t\t\'').append(i).append('\'')
        }
        sb.append('\n\t]')
        sb.append('\n\t').append('exclude').append(':').append('[')
        exclude.each { i ->
            sb.append('\n\t\t\'').append(i).append('\'')
        }
        sb.append('\n\t]\n}')
        return sb.toString()
    }
}