package com.xinzhuxiansheng.common.utils.classloader;

import com.xinzhuxiansheng.common.utils.EnumerationIter;
import com.xinzhuxiansheng.common.utils.Filter;
import com.xinzhuxiansheng.common.utils.StrUtil;
import org.apache.commons.lang3.StringUtils;
import sun.tools.jar.resources.jar;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

//classloader的工具类
public class ClassScanerUtil {
    //private static final Logger logger = LoggerFactory.getLogger(ClassScanerUtil.class);

    /**
     * 包名
     */
    private String packageName;
    /**
     * 包路径
     */
    private String packagePath;
    /** 包名，最后跟一个点，表示包名，避免在检查前缀时的歧义 */
    private String packageNameWithDot;
    /**
     * 包目录名称
     */
    private String packageDirName;
    /**
     * 过滤器
     */
    private Filter<Class<?>> classFilter;
    /**
     * 编码
     */
    private Charset charset;
    /**
     * 是否初始化类   Class.forName(...) 除了将类的.class文件加载到jvm中之外，还会对类进行解释
     * ，执行类中的static块，还会执行给静态变量赋值的静态方法
     */
    private boolean initialize;


    private Set<Class<?>> classes = new HashSet<Class<?>>();

    public ClassScanerUtil(String pageageName,Filter<Class<?>> classFilter){
        pageageName = StrUtil.nullToDefault(pageageName,"");
        this.packageName = pageageName;
        this.packageNameWithDot = StrUtil.addSuffixIfNot(packageName, ".");
        this.classFilter = classFilter;
        this.packageDirName = pageageName.replace('.',File.separatorChar);
        this.packagePath = packageName.replace('.','/');
        this.charset = StandardCharsets.UTF_8;
    }

    public static ClassLoader getContextClassLoader(){return Thread.currentThread().getContextClassLoader();}


    /**
     * 扫描指定包路径下所有指定类或接口的子类或实现类
     *
     * @param packageName 包路径
     * @param superClass 父类或接口
     * @return 类集合
     */
    public static Set<Class<?>> scanPackageBySuper(String packageName, final Class<?> superClass) throws UnsupportedEncodingException {
        return scanPackage(packageName, new Filter<Class<?>>() {
            @Override
            public boolean accept(Class<?> clazz) {
                return superClass.isAssignableFrom(clazz) && !superClass.equals(clazz);
            }
        });
    }

    /**
     * 扫描该包路径下所有的class文件
     * @param packageName
     * @param classFilter
     * @return
     * @throws UnsupportedEncodingException
     */
    public static Set<Class<?>> scanPackage(String packageName,Filter<Class<?>> classFilter) throws UnsupportedEncodingException {
        return new ClassScanerUtil(packageName,classFilter).scan();
    }


    /**
     * 获取ClassLoader
     * @return
     */
    public static ClassLoader getClassLoader(){
        ClassLoader classLoader = getContextClassLoader();
        if(null == classLoader){
            classLoader = ClassScanerUtil.class.getClassLoader();
            if(null == classLoader){
                classLoader = ClassLoader.getSystemClassLoader();
            }
        }
        return classLoader;
    }

    /**
     * 装饰者模式 Enumeration不具有foreach的特性，所以，将Enumeration 放入 EnumerationIter类中
     * EnumerationIter 实现了 Iterator<T>,Iterable<T> 接口，并且重写了 iterator,hasNext,next,remove
     * @param resource
     * @return
     */
    public EnumerationIter<URL>  getResourceIter(String resource){
        Enumeration<URL> resources;
        try {
            resources = getClassLoader().getResources(resource);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        return new EnumerationIter(resources);
    }

    public Set<Class<?>> scan() throws UnsupportedEncodingException {
        Iterator itclass = getResourceIter("").iterator();
        while(itclass.hasNext()){
            URL url = (URL) itclass.next();
            switch(url.getProtocol()){
                case "file":
                    scanFile(new File(URLDecoder.decode(url.getFile(),"UTF-8")),null);
                    break;
                case "jar":
                    scanJar(url);
                    break;
            }
        }

        return null;
    }

    /**
     * 扫描文件目录
     * @param file
     * @param rootDir
     */
    private void scanFile(File file,String rootDir){
        if (file.isFile()) {
            final String fileName = file.getAbsolutePath();
            if (fileName.endsWith(".class")) {
                final String className = fileName//
                        // 8为classes长度，fileName.length() - 6为".class"的长度
                        .substring(rootDir.length(), fileName.length() - 6)//
                        .replace(File.separatorChar,'.');//
                //加入满足条件的类
                addIfAccept(className);
            } else if (fileName.endsWith(".jar")) {
                try {
                    scanJar(new JarFile(file));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (file.isDirectory()) {
            for (File subFile : file.listFiles()) {
                scanFile(subFile, (null == rootDir) ? subPathBeforePackage(file) : rootDir);
            }
        }
    }

    private void scanJar(URL url){
        try{
            JarURLConnection urlConnection = (JarURLConnection)url.openConnection();
            JarFile jarFile =  urlConnection.getJarFile();

            String  name;

            for(JarEntry entry:new EnumerationIter<>(jarFile.entries())){
                name = StrUtil.removePrefix(entry.getName(),"/");
                if(name.startsWith(this.packagePath)){
                    if(name.endsWith(".class") && !entry.isDirectory()){
                        final String className = name.substring(0,name.length()-6).replace('/','.');
                        addIfAccept(loadClass(className));
                    }
                }
            }


        }catch(IOException e){
            e.printStackTrace();
        }
    }


    private void scanJar(JarFile jar) {
        String  name;

        for(JarEntry entry:new EnumerationIter<>(jar.entries())){
            name = StrUtil.removePrefix(entry.getName(),"/");
            if(name.startsWith(this.packagePath)){
                if(name.endsWith(".class") && !entry.isDirectory()){
                    final String className = name.substring(0,name.length()-6).replace('/','.');
                    addIfAccept(loadClass(className));
                }
            }
        }
    }

    private void addIfAccept(Class<?> clazz){
        if(null != clazz){
            if(classFilter==null|| classFilter.accept(clazz)){
                this.classes.add(clazz);
            }
        }
    }


    /**
     * 通过过滤器，是否满足接受此类的条件
     *
     * @param className
     * @return 是否接受
     */
    private void addIfAccept(String className) {
        if(StringUtils.isBlank(className)) {
            return;
        }
        int classLen = className.length();
        int packageLen = this.packageName.length();
        if(classLen == packageLen) {
            //类名和包名长度一致，用户可能传入的包名是类名
            if(className.equals(this.packageName)) {
                addIfAccept(loadClass(className));
            }
        } else if(classLen > packageLen){
            //检查类名是否以指定包名为前缀，包名后加.（避免类似于cn.hutool.A和cn.hutool.ATest这类类名引起的歧义）
            if(className.startsWith(this.packageNameWithDot)) {
                addIfAccept(loadClass(className));
            }
        }
    }

    /**
     * 加载类
     * @param className
     * @return
     */
    private Class<?> loadClass(String className){
        Class<?> clazz = null;
        try {
            clazz =  Class.forName(className,false,getClassLoader());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return clazz;
    }

    /**
     * 截取文件绝对路径中包名之前的部分
     *
     * @param file 文件
     * @return 包名之前的部分
     */
    private String subPathBeforePackage(File file) {
        String filePath = file.getAbsolutePath();
        if (StringUtils.isNotBlank(this.packageDirName)) {
            filePath = StrUtil.subBefore(filePath, this.packageDirName, true);
        }
        return StrUtil.addSuffixIfNot(filePath, File.separator);
    }


    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackagePath() {
        return packagePath;
    }

    public void setPackagePath(String packagePath) {
        this.packagePath = packagePath;
    }

    public Filter<Class<?>> getClassFilter() {
        return classFilter;
    }

    public void setClassFilter(Filter<Class<?>> classFilter) {
        this.classFilter = classFilter;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public Set<Class<?>> getClasses() {
        return classes;
    }

    public void setClasses(Set<Class<?>> classes) {
        this.classes = classes;
    }
}
