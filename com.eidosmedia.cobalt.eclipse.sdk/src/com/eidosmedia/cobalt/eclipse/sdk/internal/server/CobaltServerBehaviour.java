package com.eidosmedia.cobalt.eclipse.sdk.internal.server;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetConstants;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.eclipse.wst.server.core.util.PublishHelper;

import com.eidosmedia.cobalt.eclipse.sdk.internal.CobaltSDKPlugin;
import com.eidosmedia.cobalt.eclipse.sdk.internal.Logger;
import com.eidosmedia.cobalt.eclipse.sdk.internal.launching.CobaltLaunchConfiguration;

public class CobaltServerBehaviour extends ServerBehaviourDelegate {

    // TODO protected transient PingThread m_pingThread = null;

    protected transient IDebugEventSetListener m_processListener;

    @Override
    protected void initialize(IProgressMonitor monitor) {

    }

    @Override
    public void dispose() {

    }

    /**
     * 
     * @return
     */
    protected CobaltServer getCobaltServer() {
        IServer server = getServer();
        CobaltServer cobaltServer = (CobaltServer) server.loadAdapter(CobaltServer.class, null);
        return cobaltServer;
    }

    /**
     * 
     * @return
     */
    public IPath getBaseFolderPath() {
        CobaltServer cobaltServer = getCobaltServer();
        String baseFolder = cobaltServer.getBaseFolder();
        if (baseFolder == null || baseFolder.isEmpty()) {
            return getTempDirectory();
        } else {
            return new Path(baseFolder);
        }
    }

    public File getWebappsFolder() {
        return new File(getBaseFolderPath().toOSString(), "www/cobaltwebapps");
    }

    public File getWebfragmentsFolder() {
        return new File(getBaseFolderPath().toOSString(), "www/cobaltwebfragments");
    }

    /**
     * 
     * @return
     * @throws CoreException
     */
    public int getJMXPort() {
        CobaltServer cobaltServer = getCobaltServer();
        return cobaltServer.getJMXPort();
    }

    /**
     * 
     * @return
     */
    @Override
    public IStatus canPublish() {
        return Status.OK_STATUS;
    }

    /**
     * 
     */
    @Override
    protected void publishStart(IProgressMonitor monitor) throws CoreException {
        super.publishStart(monitor);

        try {
            IServer server = getServer();
            IRuntime runtime = server.getRuntime();

            IPath cobaltHomeIPath = runtime.getLocation();
            IPath cobaltBaseIPath = getBaseFolderPath();
            IFolder serverConfIPath = server.getServerConfiguration();
            Logger.log(Logger.INFO, String.format("publishStart cobaltHome=%s cobaltBase=%s conf=%s", cobaltHomeIPath, cobaltBaseIPath, serverConfIPath));

            File cobaltHome = new File(cobaltHomeIPath.toOSString()).getCanonicalFile();
            File cobaltBase = new File(cobaltBaseIPath.toOSString()).getCanonicalFile();
            File cobaltBaseConf = new File(cobaltBase, "conf");
            File serverConf = new File(serverConfIPath.getFolder("conf").getRawLocation().toOSString()).getCanonicalFile();
            File cobaltBaseSrc = new File(cobaltBase, "src");
            File serverSrc = new File(serverConfIPath.getFolder("src").getRawLocation().toOSString()).getCanonicalFile();
            
            File cobaltHomeExtra = new File(cobaltHome, "extra");
            File cobaltBaseExtra = new File(cobaltBase, "extra");

            if (!cobaltBase.equals(cobaltHome)) {
                FileSystemSyncExecutor syncExecutor = new FileSystemSyncExecutor();
                syncExecutor.sync(serverConf, cobaltBaseConf, monitor);
                syncExecutor.sync(serverSrc, cobaltBaseSrc, monitor);
                syncExecutor.sync(cobaltHomeExtra, cobaltBaseExtra, monitor);
            }

        } catch (Exception ex) {
            throw new CoreException(CobaltSDKPlugin.createStatus(IStatus.ERROR, "Publish server data error", ex));
        }
    }

    /**
     * 
     */
    @Override
    protected void publishServer(int kind, IProgressMonitor monitor) throws CoreException {
        // TODO Auto-generated method stub
        super.publishServer(kind, monitor);
    }

    /**
     * 
     */
    @Override
    protected void publishModule(int kind, int deltaKind, IModule[] moduleTree, IProgressMonitor monitor) throws CoreException {
        super.publishModule(kind, deltaKind, moduleTree, monitor);

        /* 
         * NOTES
         * 
         * <extension point="org.eclipse.wst.server.core.publishTasks">
         * <publishTask
         *   id="org.eclipse.jst.server.tomcat.core.publishTask"
         *   typeIds="org.eclipse.jst.server.tomcat.*"
         *   class="org.eclipse.jst.server.tomcat.core.internal.PublishTask"/>
         * </extension>
         *
         * SEE ALSO
         *
         * org.eclipse.wst.server.core.model.ServerBehaviourDelegate.publish(int, IProgressMonitor)
         * org.eclipse.jst.server.tomcat.core.internal.TomcatServerBehaviour.publishServer(int, IProgressMonitor)
         * org.eclipse.jst.server.tomcat.core.internal.PublishTask
         * org.eclipse.jst.server.tomcat.core.internal.PublishOperation2
         * 
         * HOW TO GET ADAPTER FOR THE MODULE
         * 
         * IWebModule webModule = (IWebModule) moduleTree[0].loadAdapter(IWebModule.class, monitor);
         * IJ2EEModule childModule = (IJ2EEModule) moduleTree[0].loadAdapter(IJ2EEModule.class, monitor);
         * 
         */

        try {
            String id = moduleTree[0].getModuleType().getId();
            String moduleName = moduleTree[0].getName();
//            String moduleDocBase = "${cobalt.base}/www/wtpwebapps/" + moduleName;
//            String webBaseDocBase = "${cobalt.home}/lib/modules/web-base-${cobalt.version}";
            IPath cobaltBaseIPath = getBaseFolderPath();
            File cobaltBase = new File(cobaltBaseIPath.toOSString()).getCanonicalFile();
            File cobaltTemp = new File(cobaltBase, "temp");
            File cobaltWebapps = getWebappsFolder();
            File cobaltWebfragments = getWebfragmentsFolder();

//            IFolder confFolder = getServer().getServerConfiguration();
//            IFile serverXml = confFolder.getFile("server.xml");
//            java.nio.file.Path serverXmlPath = serverXml.getLocation()
//                    .toFile()
//                    .toPath();
//
//            String content = new String(Files.readAllBytes(serverXmlPath), StandardCharsets.UTF_8);

            if (deltaKind == ServerBehaviourDelegate.REMOVED) {
                if (id.equals(IJ2EEFacetConstants.DYNAMIC_WEB)) {
                    PublishHelper.deleteDirectory(new File(cobaltWebapps, moduleName), monitor);
                } else if (id.equals(IJ2EEFacetConstants.WEBFRAGMENT)){
                    PublishHelper.deleteDirectory(new File(cobaltWebfragments, moduleName), monitor);
                }
                //content = content.replace(moduleDocBase, webBaseDocBase);
                //Files.write(serverXmlPath, content.getBytes(StandardCharsets.UTF_8));
            } else {
//                if (!content.contains(moduleDocBase)) {
//                    content = content.replace(webBaseDocBase, moduleDocBase);
//                    Files.write(serverXmlPath, content.getBytes(StandardCharsets.UTF_8));
//                }
                PublishHelper helper = new PublishHelper(cobaltTemp);
                if (id.equals(IJ2EEFacetConstants.DYNAMIC_WEB)) {
                    if (!cobaltWebapps.exists()) {
                        cobaltWebapps.mkdir();
                    }
                    IPath cobaltWebappsIPath = new Path(cobaltWebapps.getCanonicalPath());
                    IPath deployIPath = cobaltWebappsIPath.append(moduleName);

                    // TODO List<IStatus> status = new ArrayList<IStatus>();
                    if (kind == IServer.PUBLISH_CLEAN || kind == IServer.PUBLISH_FULL) {
                        IModuleResource[] mr = getResources(moduleTree);
                        /*IStatus[] stat = */helper.publishFull(mr, deployIPath, monitor);
                        // TODO addArrayToList(status, stat);
                    } else {
                        IModuleResourceDelta[] delta = getPublishedResourceDelta(moduleTree);
                        int size = delta.length;
                        for (int i = 0; i < size; i++) {
                            /*IStatus[] stat = */helper.publishDelta(delta[i], deployIPath, monitor);
                            // TODO addArrayToList(status, stat);
                        }
                    }
                } else if (id.equals(IJ2EEFacetConstants.WEBFRAGMENT)){
                    IProject project = moduleTree[0].getProject();
                    if (!cobaltWebfragments.exists()) {
                        cobaltWebfragments.mkdirs();
                    }
                    //it is a web-fragment for the web-base
                    IModuleResource[] mr = getResources(moduleTree);
                    IPath cobaltWebbaseExtensionsIPath = new Path(cobaltWebfragments.getCanonicalPath());

                    //sync libraries
                    IFolder targetFolder = project.getFolder("target");
                    if (targetFolder != null && targetFolder.exists()) {
                        IFolder dependencyFolder = targetFolder.getFolder("dependency");
                        if (dependencyFolder != null && dependencyFolder.exists()) {
                            File dependencies = Paths.get(dependencyFolder.getLocationURI()).toFile();
                            FileSystemSyncExecutor syncExecutor = new FileSystemSyncExecutor();
                            syncExecutor.sync(dependencies, new File(cobaltWebfragments, moduleName), monitor);
                        }
                    }

                    //publish jar
                    IPath deployIpath = cobaltWebbaseExtensionsIPath.append(moduleName).append(moduleName).addFileExtension("jar");
                    helper.publishZip(mr, deployIpath, monitor);

                    //ALTERNATIVE METHOD
//                    IJavaProject create = JavaCore.create(moduleTree[0].getProject());
//                    IClasspathEntry[] resolvedClasspath = create.getResolvedClasspath(true);
//                    Set<IClasspathEntry> pomCompileEntries = new HashSet<>();
//                    for (IClasspathEntry iClasspathEntry : resolvedClasspath) {
//                        if (iClasspathEntry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
//                            IClasspathAttribute[] extraAttributes = iClasspathEntry.getExtraAttributes();
//                            for (IClasspathAttribute iClasspathAttribute : extraAttributes) {
//                                if ("maven.scope".equals(iClasspathAttribute.getName()) && "compile".equals(iClasspathAttribute.getValue())) {
//                                    pomCompileEntries.add(iClasspathEntry);
//                                }
//                            }
//                        }
//                    }
//                    for (IClasspathEntry iClasspathEntry : pomCompileEntries) {
//                        java.nio.file.Path sourceLibPath = iClasspathEntry.getPath().toFile().toPath();
//                        java.nio.file.Path destinationLibPath = cobaltWebbaseExtensionsIPath.toFile().toPath().resolve(sourceLibPath.getFileName());
//                        Files.copy(sourceLibPath, destinationLibPath, StandardCopyOption.REPLACE_EXISTING);
//                    }
                }
            }

        } catch (Exception ex) {
            throw new CoreException(CobaltSDKPlugin.createStatus(IStatus.ERROR, "Publish module error", ex));
        }

        setModulePublishState(moduleTree, IServer.PUBLISH_STATE_NONE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.wst.server.core.model.ServerBehaviourDelegate#publishFinish(
     * org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected void publishFinish(IProgressMonitor monitor) throws CoreException {
        // TODO Auto-generated method stub
        super.publishFinish(monitor);
    }

    /**
     * 
     */
    @Override
    public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProgressMonitor monitor) throws CoreException {

        IServer server = getServer();

        String confFolderExpr = workingCopy.getAttribute(CobaltLaunchConfiguration.ATTR_CONFIGURATION_FOLDER, (String) null);
        if (confFolderExpr == null) {
            IFolder serverConfigurationFolder = server.getServerConfiguration();
            String confFolderPath = serverConfigurationFolder.getRawLocation().toOSString();
            workingCopy.setAttribute(CobaltLaunchConfiguration.ATTR_CONFIGURATION_FOLDER, confFolderPath);
        }
    }

    /**
     * 
     */
    @Override
    public void stop(boolean force) {
        setServerState(IServer.STATE_STOPPING);
        try {
            ILaunchConfiguration launchConfig = getServer().getLaunchConfiguration(true, null);
            ILaunchConfigurationWorkingCopy wc = launchConfig.getWorkingCopy();
            wc.setAttribute(IDebugUIConstants.ATTR_PRIVATE, true);
            wc.setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, true);
            wc.setAttribute(CobaltLaunchConfiguration.ATTR_COBALT_SHUTDOWN, true);
            wc.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor());
        } catch (Exception ex) {
            Logger.log(IStatus.ERROR, "Stop portal error", ex);
        }
        setServerState(IServer.STATE_STOPPED);
    }

    /**
     * List all projects names added to this server.
     * 
     * @return
     */
    public List<String> getAllExtensionProjects() {
        IServer server = getServer();
        List<String> projects = new ArrayList<String>();
        IModule[] modules = server.getModules();
        for (IModule module : modules) {
            IProject project = module.getProject();
            String name = project.getName();
            projects.add(name);
        }
        return projects;
    }

    /**
     * Prepare the server just before to be launched.
     * 
     * @param launch
     * @param launchMode
     * @param monitor
     * @throws CoreException
     */
    public void setupLaunch(ILaunch launch, String launchMode, IProgressMonitor monitor) throws CoreException {

        // TODO use STATE_STARTING and start ping thread
        setServerRestartState(false);
        setServerState(IServer.STATE_STARTED);
        setMode(launchMode);
    }

    /**
     * 
     * @param newProcess
     */
    public void addProcessListener(final IProcess newProcess) {
        if (m_processListener != null || newProcess == null)
            return;
        m_processListener = new IDebugEventSetListener() {

            public void handleDebugEvents(DebugEvent[] events) {
                if (events != null) {
                    int size = events.length;
                    for (int i = 0; i < size; i++) {
                        if (newProcess != null && newProcess.equals(events[i].getSource()) && events[i].getKind() == DebugEvent.TERMINATE) {
                            stopImpl();
                        }
                    }
                }
            }
        };
        DebugPlugin.getDefault().addDebugEventListener(m_processListener);
    }

    /**
     * Terminates the server.
     */
    public void terminate() {
        if (getServer().getServerState() == IServer.STATE_STOPPED)
            return;
        try {
            setServerState(IServer.STATE_STOPPING);
            ILaunch launch = getServer().getLaunch();
            if (launch != null) {
                launch.terminate();
                stopImpl();
            }
        } catch (Exception ex) {
            Logger.log(IStatus.ERROR, "Error killing the process", ex);
        }
    }

    /**
     * 
     */
    public void stopImpl() {
        if (m_processListener != null) {
            DebugPlugin.getDefault().removeDebugEventListener(m_processListener);
            m_processListener = null;
        }
        setServerState(IServer.STATE_STOPPED);
    }
}
