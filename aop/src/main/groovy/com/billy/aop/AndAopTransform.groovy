package com.billy.aop

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.apache.commons.codec.digest.*
import org.objectweb.asm.*

/**
 *
 * @author billy.qi
 * @since 17/3/21 11:48
 */
class AndAopTransform extends Transform {

    Project project
    public AndAopTransform(Project project) {
        this.project = project
    }


    @Override
    String getName() {
        return "andAop"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    boolean isIncremental() {
        return false;
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs
                   , Collection<TransformInput> referencedInputs
                   , TransformOutputProvider outputProvider
                   , boolean isIncremental) throws IOException, TransformException, InterruptedException {
        project.logger.warn("start andAop transform...")

        /**
         * 遍历输入文件
         */
        inputs.each { TransformInput input ->

            /**
             * 遍历jar
             */
            input.jarInputs.each { JarInput jarInput ->
                String destName = jarInput.name;
                /**
                 * 重名名输出文件,因为可能同名,会覆盖
                 */
                def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath);
                if (destName.endsWith(".jar")) {
                    destName = destName.substring(0, destName.length() - 4);
                }
                // 获得输入文件
                File src = jarInput.file
                // 获得输出文件
                File dest = outputProvider.getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR);


                //处理jar进行字节码注入处理TODO
                if (AndAopProcessor.shouldProcessPreDexJar(src.absolutePath)) {
                    AndAopProcessor.processJar(src)
                }

                FileUtils.copyFile(src, dest);

                project.logger.info "Copying\t${src.absolutePath} \nto\t\t${dest.absolutePath}"
            }
            /**
             * 遍历目录
             */
            input.directoryInputs.each { DirectoryInput directoryInput ->
                /**
                 * 获得产物的目录
                 */
                File dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY);
                String buildTypes = directoryInput.file.name
                String productFlavors = directoryInput.file.parentFile.name
                //这里进行我们的处理 TODO
                println 'path:' + project.path
                println("buildTypes:" + buildTypes)
                String root = directoryInput.file.absolutePath
                if (!root.endsWith(File.separator))
                    root += File.separator
                //遍历目录下的每个文件
                directoryInput.file.eachFileRecurse { File file ->
                    def path = file.absolutePath.replace(root, '')
                    if(file.isFile() && AndAopProcessor.shouldProcessClass(path)){
                        project.logger.error('andAop class:' + file.absolutePath)
                        AndAopProcessor.processClass(file)
                    }
                }
                project.logger.info "Copying\t${directoryInput.file.absolutePath} \nto\t\t${dest.absolutePath}"
                /**
                 * 处理完后拷到目标文件
                 */
                FileUtils.copyDirectory(directoryInput.file, dest);
            }
        }
    }

}
