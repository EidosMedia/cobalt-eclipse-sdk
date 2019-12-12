package com.eidosmedia.cobalt.eclipse.sdk.internal.launching;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jdt.launching.sourcelookup.containers.JavaProjectSourceContainer;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;

import com.eidosmedia.cobalt.eclipse.sdk.internal.CobaltSDKPlugin;
import com.eidosmedia.cobalt.eclipse.sdk.internal.Logger;
import com.eidosmedia.cobalt.eclipse.sdk.internal.Messages;
import com.eidosmedia.cobalt.eclipse.sdk.internal.server.CobaltRuntimeConfigurationData;
import com.eidosmedia.cobalt.eclipse.sdk.internal.server.CobaltServer;
import com.eidosmedia.cobalt.eclipse.sdk.internal.server.CobaltServerBehaviour;
import com.eidosmedia.cobalt.eclipse.sdk.internal.server.ICobaltRuntime;

public class CobaltLaunchConfiguration extends AbstractJavaLaunchConfigurationDelegate {

    private static final String OS = System.getProperty("os.name").toLowerCase();

    public static final ComparableVersion V_2_7_0_SNAPSHOT = new ComparableVersion("2.7.0-SNAPSHOT");

    public static final String ATTR_CONFIGURATION_FOLDER =
        "com.eidosmedia.cobalt.eclipse.sdk.launching.ATTR_CONFIGURATION_FOLDER";

    public static final String ATTR_JMX_PORT = "com.eidosmedia.cobalt.eclipse.sdk.launching.ATTR_JMX_PORT";

    public static final String ATTR_COBALT_SHUTDOWN =
        "com.eidosmedia.cobalt.eclipse.sdk.launching.ATTR_COBALT_SHUTDOWN";

    @Override
    public void launch(ILaunchConfiguration configuration, String launchMode, ILaunch launch, IProgressMonitor monitor)
        throws CoreException {

        // Eval server configuration

        String serverId = configuration.getAttribute("server-id", (String) null);
        IServer server = ServerCore.findServer(serverId);
        if (server == null) {
            CobaltSDKPlugin.warning(Messages.DefaultTitle, "Invalid Cobalt server.");
            return;
        }

        CobaltServerBehaviour cobaltServer =
            (CobaltServerBehaviour) server.loadAdapter(CobaltServerBehaviour.class, null);

        // Eval Cobalt runtime

        IRuntime runtime = server.getRuntime();
        ICobaltRuntime cobaltRuntime = (ICobaltRuntime) runtime.loadAdapter(ICobaltRuntime.class, null);
        if (cobaltRuntime == null) {
            CobaltSDKPlugin.warning(Messages.DefaultTitle, "Invalid Cobalt runtime.");
            return;
        }

        CobaltRuntimeConfigurationData runtimeConfigurationData;
        try {
            runtimeConfigurationData = cobaltRuntime.getConfigurationData();
        } catch (Exception ex) {
            CobaltSDKPlugin.warning(Messages.DefaultTitle, "Eval Cobalt runtime configuration data error: " + ex);
            return;
        }

        // Check dev workspace

        boolean isDevWorkspace = cobaltRuntime.isDevWorkspace();
        if (isDevWorkspace) {
            CobaltSDKPlugin.warning(Messages.DefaultTitle, "Launch Cobalt not enabled in developing workspace.");
            return;
        }

        // Startup folders

        String cobaltHome = runtime.getLocation().toOSString();
        if (cobaltHome == null || cobaltHome.isEmpty()) {
            CobaltSDKPlugin.warning(Messages.DefaultTitle, "Cobalt SDK not configured.");
            return;
        }

        File catalinaHomeFolder = new File(cobaltHome, "/servers/apache-tomcat"); //$NON-NLS-1$
        String catalinaHomePath = catalinaHomeFolder.getAbsolutePath();
        File libFolder = new File(cobaltHome, "lib");
        String libFolderPath = libFolder.getAbsolutePath();
        IPath serverBaseIPath = cobaltServer.getBaseFolderPath();
        String serverBasePath = serverBaseIPath.toOSString();
        File tmpFolder = new File(serverBasePath, "temp");
        String tmpFolderPath = tmpFolder.getAbsolutePath();
        File dataFolder = new File(serverBasePath, "data");
        File cobaltWebappsFolder = cobaltServer.getWebappsFolder();
        String cobaltWebappsFolderPath = cobaltWebappsFolder.getAbsolutePath();
        File cobaltWebfragmentsFolder = cobaltServer.getWebfragmentsFolder();
        String cobaltWebfragmentsFolderPath = cobaltWebfragmentsFolder.getAbsolutePath();
        String serverDataPath = server.getAttribute(CobaltServer.ATTR_DATA_PATH, (String) null);
        if (serverDataPath != null && !serverDataPath.trim().isEmpty()) {
            dataFolder = new File(serverDataPath);
        }
        String dataFolderPath = dataFolder.getAbsolutePath();

        String pathSepartor = File.pathSeparator;

        String reposPath = libFolderPath + "/repo";

        File workFolder = new File(serverBasePath, "work");
        String workFolderPath = workFolder.getAbsolutePath();
        if (!workFolder.exists()) {
            workFolder.mkdirs();
        }

        // Eval configuration folder

        IFolder serverConfIFolder = server.getServerConfiguration();
        String serverConfFolderPath = serverConfIFolder.getFolder("conf").getRawLocation().toOSString();
        File serverConfFolder = new File(serverConfFolderPath);
        String serverConfPath = server.getAttribute(CobaltServer.ATTR_CONF_PATH, (String) null);
        if (serverConfPath != null && !serverConfPath.trim().isEmpty()) {
            serverConfFolder = new File(serverConfPath);
            serverConfFolderPath = serverConfFolder.getAbsolutePath();
        }
        if (!serverConfFolder.exists()) {
            CobaltSDKPlugin.warning(Messages.DefaultTitle,
                                    "Configuration folder does not exist: " + serverConfFolderPath + ".");
            return;
        }

        String serverSrcFolderPath = serverConfIFolder.getFolder("src").getRawLocation().toOSString();
        File serverSrcFolder = new File(serverSrcFolderPath);
        String serverSrcPath = server.getAttribute(CobaltServer.ATTR_SRC_PATH, (String) null);
        if (serverSrcPath != null && !serverSrcPath.trim().isEmpty()) {
            serverSrcFolder = new File(serverSrcPath);
            serverSrcFolderPath = serverSrcFolder.getAbsolutePath();
        }
        if (!serverSrcFolder.exists()) {
            CobaltSDKPlugin.warning(Messages.DefaultTitle,
                                    "Sources folder does not exist: " + serverSrcFolderPath + ".");
            return;
        }

        File cobaltConfFolder = new File(serverConfFolderPath, "cobalt");
        if (!cobaltConfFolder.exists()) {
            CobaltSDKPlugin.warning(Messages.DefaultTitle,
                                    "Cobalt configuration folder does not exist: " + serverConfFolderPath + ".");
            return;
        }

        File commonPropertiesFolder = new File(serverConfFolderPath, "cobalt.properties");
        String commonPropertiesPath = commonPropertiesFolder.getAbsolutePath();

        Properties cobaltProperties = new Properties();

        try {
            cobaltProperties.load(new FileInputStream(commonPropertiesFolder));
        } catch (FileNotFoundException e) {
            CobaltSDKPlugin.warning(Messages.DefaultTitle, "Error reading file: cobalt.properties.");
            return;
        } catch (IOException e) {
            CobaltSDKPlugin.warning(Messages.DefaultTitle, "Error reading file: cobalt.properties.");
            return;
        }

        File buildPropertiesFile = new File(cobaltHome, "bin/build.properties");
        
        Properties buildProperties = new Properties();

        try {
            buildProperties.load(new FileInputStream(buildPropertiesFile));
        } catch (FileNotFoundException e) {
            CobaltSDKPlugin.warning(Messages.DefaultTitle, "Error reading file: build.properties.");
            return;
        } catch (IOException e) {
            CobaltSDKPlugin.warning(Messages.DefaultTitle, "Error reading file: build.properties.");
            return;
        }
        
        String cobaltVersion = buildProperties.getProperty("build.version");
        if (new ComparableVersion(cobaltVersion).compareTo(V_2_7_0_SNAPSHOT) >= 0) {
            String elasticVersion = cobaltProperties.getProperty("repository.search.elastic.version", "54");
            String elasticClassesPath = libFolderPath + "/es" + elasticVersion;
            reposPath = reposPath + pathSepartor + elasticClassesPath;
        }
        String servicesPath =
            libFolderPath + "/services/" + pathSepartor + libFolderPath + "/modules/services-" + cobaltVersion + ".jar";

        // Eval classpath

        List<String> classpathItems = new ArrayList<String>();
        // FIXME String[] userClasspath = getUserClasspath(configuration);
        File binFolder = new File(catalinaHomeFolder, "bin"); //$NON-NLS-1$
        for (File jar : binFolder.listFiles()) {
            if (jar.getName().toLowerCase().endsWith(".jar")) {
                classpathItems.add(escapeClassPathEntry(jar.getAbsolutePath()));
            }
        }

        List<ISourceContainer> sourceContainers = new ArrayList<ISourceContainer>();

        // VM and Program args

        String vmArgs = null;
        String pgmArgs = "-config \"" + serverConfFolderPath + File.separator + "server.xml\" ";

        boolean stopServer = configuration.getAttribute(ATTR_COBALT_SHUTDOWN, false);

        // Classpath

        //classpathList.add(escapeClassPathEntry(serverConfFolderPath));

        // Collect extension projects

        try {

            List<String> modulesProjects = cobaltServer.getAllExtensionProjects();
            String mainProjectName =
                configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
            if (mainProjectName != null) {
                modulesProjects.add(mainProjectName);
            }

            IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
            IJavaModel javaModel = JavaCore.create(workspaceRoot);
            for (String projectName : modulesProjects) {
                IJavaProject javaProject = javaModel.getJavaProject(projectName);
                JavaProjectSourceContainer sourceContainer = new JavaProjectSourceContainer(javaProject);
                sourceContainers.add(sourceContainer);
            }

        } catch (final Exception ex) {
            String message = "Build extensions classpath error";
            Logger.log(Logger.ERROR, message, ex);
            CobaltSDKPlugin.warning(Messages.DefaultTitle, message + ".\n" + ex);
            return;
        }

        // Tomcat catalina.policy 

        String catalinaPolicyPath = null;
        File catalinaPolicy = new File(serverConfFolderPath, "catalina.policy");
        if (catalinaPolicy.exists()) {
            catalinaPolicyPath = catalinaPolicy.getAbsolutePath();
        }

        // Tomcat logging.properties

        String loggingPropertiesPath = null;
        File loggingProperties = new File(serverConfFolderPath, "logging.properties");
        if (!loggingProperties.exists()) {
            loggingProperties = new File(cobaltHome, "conf/logging.properties");
        }
        if (loggingProperties.exists()) {
            loggingPropertiesPath = loggingProperties.getAbsolutePath();
        }

        // Specific Cobalt configuration

        int jmxPort = configuration.getAttribute(ATTR_JMX_PORT, CobaltServer.DEFAULT_JMX_PORT);

        // Program & VM args

        vmArgs = removeCRLF(getVMArguments(configuration));
        vmArgs = appendVMArg(vmArgs, "-server", "-server");
        // vmArgs = appendVMArg(vmArgs, "-Dfile.encoding=\"UTF-8\"", "-Dfile.encoding=");           
        vmArgs = appendVMArg(vmArgs, "-Djava.endorsed.dirs=\"endorsed\"", "-Djava.endorsed.dirs=");
        vmArgs = appendVMArg(vmArgs, "-XX:MaxPermSize=256m", "-XX:MaxPermSize=");
        vmArgs = appendVMArg(vmArgs, "-Xms512m", "-Xms\\d+[kKmM]?");
        vmArgs = appendVMArg(vmArgs, "-Xmx512m", "-Xmx\\d+[kKmM]?");
        vmArgs = appendVMArg(vmArgs, "-Djava.net.preferIPv4Stack=true", "-Djava.net.preferIPv4Stack=");
        vmArgs = appendVMArg(vmArgs, "-Djava.awt.headless=true", "-Djava.awt.headless=");
        if (catalinaPolicyPath != null) {
            // FIXME catalina.policy
            //vmArgs = appendVMArg(vmArgs, "-Djava.security.manager", "-Djava.security.manager");
            //vmArgs = appendVMArg(vmArgs, "-Djava.security.policy=\"" + catalinaPolicyPath + "\"", "-Djava.security.policy=");
        }
        if (loggingPropertiesPath != null) {
            vmArgs = appendVMArg(vmArgs, "-Djava.util.logging.config.file=\"" + loggingPropertiesPath + "\"",
                                 "-Djava.util.logging.config.file=");
            vmArgs = appendVMArg(vmArgs, "-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager",
                                 "-Djava.util.logging.manager=");
        }

        String tomcatKeystoreFile = cobaltProperties.getProperty("tomcat.keystore.file");
        String tomcatKeystorePass = cobaltProperties.getProperty("tomcat.keystore.pass");
        String tomcatKeyAlias = cobaltProperties.getProperty("tomcat.key.alias");
        String tomcatKeyPass = cobaltProperties.getProperty("tomcat.key.pass");

        String tomcatPortServer = cobaltProperties.getProperty("ports.tomcat.server");
        String tomcatPortAjp = cobaltProperties.getProperty("ports.tomcat.ajp");
        String tomcatPortHttp = cobaltProperties.getProperty("ports.tomcat.http");
        String tomcatPortHttps = cobaltProperties.getProperty("ports.tomcat.https");
        String tomcatPortRedirect = cobaltProperties.getProperty("ports.tomcat.redirect");

        vmArgs = appendVMArg(vmArgs, "-Dcobalt.home=\"" + cobaltHome + "\"", "-Dcobalt.home=");
        vmArgs = appendVMArg(vmArgs, "-Dcobalt.base=\"" + serverBasePath + "\"", "-Dcobalt.base=");
        vmArgs = appendVMArg(vmArgs, "-Dem.conf.path=\"" + cobaltConfFolder + "\"", "-Dem.conf.path=");
        vmArgs = appendVMArg(vmArgs, "-Dem.conf.common=\"" + commonPropertiesPath + "\"", "-Dem.conf.common=");
        vmArgs = appendVMArg(vmArgs, "-Dem.data.path=\"" + dataFolderPath + "\"", "-Dem.data.path=");
        vmArgs = appendVMArg(vmArgs, "-Dcobalt.webapps=\"" + cobaltWebappsFolderPath + "\"", "-Dcobalt.webapps=");
        vmArgs = appendVMArg(vmArgs, "-Dcobalt.webfragments=\"" + cobaltWebfragmentsFolderPath + "\"", "-Dcobalt.webfragments=");
        vmArgs = appendVMArg(vmArgs, "-Dem.src.path=\"" + serverSrcFolderPath + "\"", "-Dem.src.path=");
        vmArgs = appendVMArg(vmArgs, "-Dem.work.path=\"" + workFolderPath + "\"", "-Dem.work.path=");
        vmArgs = appendVMArg(vmArgs, "-Dcobalt.version=\"" + cobaltVersion + "\"", "-Dcobalt.version=");
        vmArgs =
            appendVMArg(vmArgs, "-Dtomcat.keystore.file=\"" + tomcatKeystoreFile + "\"", "-Dtomcat.keystore.file=");
        vmArgs =
            appendVMArg(vmArgs, "-Dtomcat.keystore.pass=\"" + tomcatKeystorePass + "\"", "-Dtomcat.keystore.pass=");
        vmArgs =
            appendVMArg(vmArgs, "-Dtomcat.key.alias=\"" + tomcatKeyAlias + "\"", "-Dtomcat.key.alias=");
        vmArgs =
            appendVMArg(vmArgs, "-Dtomcat.key.pass=\"" + tomcatKeyPass + "\"", "-Dtomcat.key.pass=");


        vmArgs = appendVMArg(vmArgs, "-Dtomcat.port.server=\"" + tomcatPortServer + "\"", "-Dtomcat.port.server=");
        vmArgs = appendVMArg(vmArgs, "-Dtomcat.port.ajp=\"" + tomcatPortAjp + "\"", "-Dtomcat.port.ajp=");
        vmArgs = appendVMArg(vmArgs, "-Dtomcat.port.http=\"" + tomcatPortHttp + "\"", "-Dtomcat.port.http=");
        vmArgs = appendVMArg(vmArgs, "-Dtomcat.port.https=\"" + tomcatPortHttps + "\"", "-Dtomcat.port.https=");
        vmArgs =
            appendVMArg(vmArgs, "-Dtomcat.port.redirect=\"" + tomcatPortRedirect + "\"", "-Dtomcat.port.redirect=");

        vmArgs = appendVMArg(vmArgs, "-Dcatalina.home=\"" + catalinaHomePath + "\"", "-Dcatalina.home=");
        vmArgs = appendVMArg(vmArgs, "-Dcatalina.base=\"" + serverBasePath + "\"", "-Dcatalina.base=");
        vmArgs = appendVMArg(vmArgs, "-Dcatalina.tmpdir=\"" + tmpFolderPath + "\"", "-Dcatalina.tmpdir=");

        //            vmArgs = appendVMArg(vmArgs, "-Dtomcat.port.server=\"8005\"", "-Dtomcat.port.server=");
        //            vmArgs = appendVMArg(vmArgs, "-Dtomcat.port.ajp=\"8009\"", "-Dtomcat.port.ajp=");
        //            vmArgs = appendVMArg(vmArgs, "-Dtomcat.port.http=\"8080\"", "-Dtomcat.port.http=");
        //            vmArgs = appendVMArg(vmArgs, "-Dtomcat.port.redirect=\"8443\"", "-Dtomcat.port.redirect=");

        vmArgs = appendVMArg(vmArgs, "-Dem.services.loader=\"" + servicesPath + "\"", "-Dem.services.loader=");
        vmArgs = appendVMArg(vmArgs, "-Dem.repository.loader=\"" + reposPath + "\"", "-Dem.repository.loader=");

        // TODO -Dorg.apache.el.parser.SKIP_IDENTIFIER_CHECK=true

        // default: String pgmArgs = getProgramArguments(configuration);

        if (stopServer) {
            pgmArgs += "stop";
        } else {
            pgmArgs += "start";
            vmArgs = appendVMArg(vmArgs, "-Dcom.sun.management.jmxremote", "-Dcom.sun.management.jmxremote");
            vmArgs = appendVMArg(vmArgs, "-Dcom.sun.management.jmxremote.port=" + jmxPort,
                                 "-Dcom.sun.management.jmxremote.port=");
            vmArgs = appendVMArg(vmArgs, "-Dcom.sun.management.jmxremote.authenticate=false",
                                 "-Dcom.sun.management.jmxremote.authenticate=");
            vmArgs =
                appendVMArg(vmArgs, "-Dcom.sun.management.jmxremote.ssl=false", "-Dcom.sun.management.jmxremote.ssl=");
        }

        ExecutionArguments execArgs = new ExecutionArguments(vmArgs, pgmArgs);

        // Environments

        String[] envp = getEnvironment(configuration);
        envp = setEnvironment(envp, runtimeConfigurationData.getCatalinaBasePath());

        // VM-specific attributes

        Map<String, Object> vmAttributesMap = getVMSpecificAttributesMap(configuration);

        // Bootpath

        String[] bootpath = getBootpath(configuration);

        //

        String[] classpath = classpathItems.toArray(new String[classpathItems.size()]);
        VMRunnerConfiguration runConfig = new VMRunnerConfiguration("org.apache.catalina.startup.Bootstrap", classpath);
        runConfig.setProgramArguments(execArgs.getProgramArgumentsArray());
        runConfig.setVMArguments(execArgs.getVMArgumentsArray());
        runConfig.setWorkingDirectory(workFolderPath);
        runConfig.setEnvironment(envp);
        runConfig.setVMSpecificAttributesMap(vmAttributesMap);
        if (bootpath != null && bootpath.length > 0) {
            runConfig.setBootClassPath(bootpath);
        }

        // Source

        setDefaultSourceLocator(launch, configuration);
        ISourceLookupDirector sourceLookupDirector = (ISourceLookupDirector) launch.getSourceLocator();
        List<ISourceContainer> launchSourceContainers =
            new ArrayList<ISourceContainer>(Arrays.asList(sourceLookupDirector.getSourceContainers()));
        for (ISourceContainer c : sourceContainers) {
            if (!launchSourceContainers.contains(c))
                launchSourceContainers.add(c);
        }
        sourceLookupDirector.setSourceContainers(launchSourceContainers
                .toArray(new ISourceContainer[launchSourceContainers.size()]));

        // Setup runner

        IVMInstall vm = verifyVMInstall(configuration);
        IVMRunner runner = vm.getVMRunner(launchMode);
        if (runner == null) {
            launchMode = ILaunchManager.RUN_MODE;
            runner = vm.getVMRunner(ILaunchManager.RUN_MODE);
        }

        // Run

        cobaltServer.setupLaunch(launch, launchMode, monitor);
        try {
            runner.run(runConfig, launch, monitor);
            cobaltServer.addProcessListener(launch.getProcesses()[0]);
        } catch (Exception ex) {
            Logger.log(Logger.ERROR, "Starting Cobalt server error", ex);
            cobaltServer.stopImpl();
        }
    }

    private String escapeClassPathEntry(String path) {
        if (path.contains(" ")) {
            return "\"" + path + "\"";
        } else {
            return path;
        }
    }

    private String[] setEnvironment(String[] envp, String basePath) {
        String os = Platform.getOS();
        String imageToolPath = basePath + "/tools/";
        final String pathSuffix;
        LinkedHashMap<String, String> vars = new LinkedHashMap<String, String>();
        if (OS.indexOf("win") >= 0) {
            pathSuffix = "Windows";
        } else if (OS.indexOf("mac") >= 0) {
            pathSuffix = "Darwin";
        } else if (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0) {
            pathSuffix = "Linux64-rh5";
        } else {
            pathSuffix = null;
        }
        if (pathSuffix != null) {
            Path imageToolOSPath = null;
            try (Stream<Path> stream =
                Files.find(Paths.get(imageToolPath), 1,
                           (path, attr) -> String.valueOf(path).endsWith(pathSuffix) && attr.isDirectory())) {
                Optional<Path> first = stream.findFirst();
                imageToolOSPath = first.orElse(null);
            } catch (IOException ex) {
                Logger.log(Logger.ERROR, "Can't find imagetool for " + pathSuffix, ex);
                imageToolOSPath = null;
            }
            if (imageToolOSPath != null) {
                try {
                    File imageToolFile = imageToolOSPath.resolve("imgtool").toFile();
                    if (imageToolFile.exists()) {
                        imageToolFile.setExecutable(true, false);
                    }
                } catch (Exception ex) {
                    Logger.log(Logger.ERROR, "Unable to set execution permission on imgtool", ex);
                }
                if (envp != null) {
                    for (String str : envp) {
                        String[] var = str.split("=");
                        vars.put(var[0], var[1]);
                    }
                }
                String path = vars.get("PATH");
                if (path == null) {
                    path = System.getenv("PATH");
                }
                if (path == null) {
                    vars.put("PATH", imageToolOSPath.toString());
                } else {
                    vars.put("PATH", path + ":" + imageToolOSPath.toString());
                }
            }
        }

        String lang = vars.get("LANG");
        if (lang == null && !os.equals(Platform.OS_WIN32)) {
            String locale = Locale.US.toString();
            String charset = Charset.forName("UTF-8").toString();
            vars.put("LANG", locale + "." + charset);
        }
        envp = new String[vars.size()];

        Iterator<String> iterator = vars.keySet().iterator();
        for (int i = 0; i < envp.length; i++) {
            String key = iterator.next();
            envp[i] = key + "=" + vars.get(key);
        }

        return envp;
    }

    private String removeCRLF(String str) {
        return str.replaceAll("[ ]*([\\r\\n][ ]*)+", " ");
    }

    private String appendVMArg(String vmArgs, String arg, String ifNotMatch) {
        if (ifNotMatch != null) {
            StringBuilder sb = new StringBuilder("(?i).*");
            sb.append(ifNotMatch);
            sb.append(".*");
            if (vmArgs.matches(sb.toString()))
                return vmArgs;
        }
        StringBuilder sb = new StringBuilder(vmArgs);
        if (!vmArgs.isEmpty() && !vmArgs.endsWith(" ") && !arg.startsWith(" "))
            sb.append(" ");
        sb.append(arg);
        return sb.toString();
    }

    /**
     * FIXME implement getUserClasspath
     * 
     * @param configuration
     * @return
     * @throws CoreException
     */
    public String[] getUserClasspath(ILaunchConfiguration configuration) throws CoreException {

        IRuntimeClasspathEntry[] entries = JavaRuntime.computeUnresolvedRuntimeClasspath(configuration);

        // remove default classpath entries
        List<IRuntimeClasspathEntry> classpathEntries = new ArrayList<IRuntimeClasspathEntry>(entries.length);
        for (IRuntimeClasspathEntry entry : entries) {
            System.out.println(entry.getType() + " : " + entry);
        }
        entries = classpathEntries.toArray(new IRuntimeClasspathEntry[classpathEntries.size()]);

        // get user libraries only
        entries = JavaRuntime.resolveRuntimeClasspath(entries, configuration);
        List<String> userEntries = new ArrayList<String>(entries.length);
        Set<String> set = new HashSet<String>(entries.length);
        for (int i = 0; i < entries.length; i++) {
            if (entries[i].getClasspathProperty() == IRuntimeClasspathEntry.USER_CLASSES) {
                String location = entries[i].getLocation();
                if (location != null) {
                    if (!set.contains(location)) {
                        userEntries.add(location);
                        set.add(location);
                    }
                }
            }
        }
        return userEntries.toArray(new String[userEntries.size()]);
    }

}
