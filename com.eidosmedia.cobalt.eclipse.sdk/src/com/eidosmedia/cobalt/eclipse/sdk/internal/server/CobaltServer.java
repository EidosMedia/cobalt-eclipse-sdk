package com.eidosmedia.cobalt.eclipse.sdk.internal.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetConstants;
import org.eclipse.jst.server.core.IWebModule;
import org.eclipse.jst.server.core.internal.J2EEUtil;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleType;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerPort;
import org.eclipse.wst.server.core.model.ServerDelegate;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.eidosmedia.cobalt.eclipse.sdk.internal.CobaltSDKPlugin;
import com.eidosmedia.cobalt.eclipse.sdk.internal.actions.NewConfigurationFolderAction;

public class CobaltServer extends ServerDelegate {

    private static final String HELP_COMMENT = "To activate the context remove the .sample suffix and name it as the desired context path (\"ROOT.xml\" for the root context)";

    public static final String ATTR_BASE_FOLDER = "com.eidosmedia.cobalt.eclipse.sdk.internal.server.CobaltServer.ATTR_BASE_FOLDER";

    public static final String ATTR_JMX_PORT = "com.eidosmedia.cobalt.eclipse.sdk.internal.server.CobaltServer.ATTR_JMX_PORT";

    public static final String ATTR_DATA_PATH = CobaltServer.class.getName() + ".ATTR_DATA_PATH";

    public static final String ATTR_CONF_PATH = CobaltServer.class.getName() + ".ATTR_CONF_PATH";

    public static final String ATTR_SRC_PATH = CobaltServer.class.getName() + ".ATTR_SRC_PATH";

    public static final int DEFAULT_JMX_PORT = 9999;

    /**
     * 
     * @return
     * @throws CoreException
     */
    public String getBaseFolder() {
        IServer server = getServer();
        String baseFolder = server.getAttribute(ATTR_BASE_FOLDER, (String) null);
        return baseFolder;
    }

    /**
     * 
     * @return
     * @throws CoreException
     */
    public int getJMXPort() {
        IServer server = getServer();
        // ILaunchConfiguration launchConfiguration = server.getLaunchConfiguration(true, null);
        // int port = launchConfiguration.getAttribute(CobaltLaunchConfiguration.ATTR_JMX_PORT, CobaltServer.DEFAULT_JMX_PORT);
        int port = server.getAttribute(ATTR_JMX_PORT, DEFAULT_JMX_PORT);
        return port;
    }

    @Override
    protected void initialize() {

    }

    @Override
    public void dispose() {

    }

    @Override
    public void importRuntimeConfiguration(IRuntime runtime, IProgressMonitor monitor) throws CoreException {

        String runtimePath = runtime.getLocation().toOSString();

        IFolder serverConfigurationFolder = getServer().getServerConfiguration();
        if (serverConfigurationFolder == null) {
            // TODO serverConfigurationFolder == null
        } else {
            try {
                NewConfigurationFolderAction.copyConfigurationFolder(runtimePath, serverConfigurationFolder);
                NewConfigurationFolderAction.copySrcFolder(runtimePath, serverConfigurationFolder);
            } catch (Exception ex) {
                throw new CoreException(new Status(IStatus.ERROR, CobaltSDKPlugin.PLUGIN_ID, "Init configuration error", ex));
            }
        }
    }

    @Override
    public ServerPort[] getServerPorts() {
        // FIXME implement getServerPorts
        return null;
    }

    @Override
    public IStatus canModifyModules(IModule[] add, IModule[] remove) {
        return Status.OK_STATUS;
    }

    @Override
    public IModule[] getChildModules(IModule[] module) {
        if (module == null) {
            return null;
        }
        IModuleType moduleType = module[0].getModuleType();
        if (module.length == 1 && moduleType != null && IJ2EEFacetConstants.DYNAMIC_WEB.equals(moduleType.getId())) {
            IWebModule webModule = (IWebModule) module[0].loadAdapter(IWebModule.class, null);
            if (webModule != null) {
                IModule[] modules = webModule.getModules();
                return modules;
            }
        }
        return new IModule[0];
    }

    @Override
    public IModule[] getRootModules(IModule module) throws CoreException {
        String id = module.getModuleType().getId();
        if (IJ2EEFacetConstants.DYNAMIC_WEB.equals(id) || IJ2EEFacetConstants.WEBFRAGMENT.equals(id)) {
            IStatus status = canModifyModules(new IModule[] { module }, null);
            if (status == null || !status.isOK())
                throw new CoreException(status);
            return new IModule[] { module };
        }
        return J2EEUtil.getWebModules(module, null);
    }

    @Override
    public void modifyModules(IModule[] add, IModule[] remove, IProgressMonitor monitor) throws CoreException {
        System.out.println(" >> modifyModules");
        IFolder serverConfiguration = getServer().getServerConfiguration();
        if (remove != null) {
            for (IModule iModule : remove) {
                String id = iModule.getModuleType().getId();
                if (IJ2EEFacetConstants.DYNAMIC_WEB.equals(id)) {
                    removeWeb(monitor, serverConfiguration, iModule);
                }
                if (IJ2EEFacetConstants.WEBFRAGMENT.equals(id)) {
                    updateWebFragment(monitor, serverConfiguration, iModule, true);
                }
            }
        }
        if (add != null) {
            for (IModule iModule : add) {
                String id = iModule.getModuleType().getId();
                if (IJ2EEFacetConstants.DYNAMIC_WEB.equals(id)) {
                    addWeb(monitor, serverConfiguration, iModule);
                }
                if (IJ2EEFacetConstants.WEBFRAGMENT.equals(id)) {
                    updateWebFragment(monitor, serverConfiguration, iModule, false);
                }
            }
        }
    }

    private void updateWebFragment(IProgressMonitor monitor, IFolder serverConfiguration, IModule iModule, boolean remove) throws CoreException {
        IMavenProjectRegistry projectRegistry = MavenPlugin.getMavenProjectRegistry();
        IProject project = iModule.getProject();
        IMavenProjectFacade facade = projectRegistry.getProject(project);
        if (facade != null) {
            MavenProject p = facade.getMavenProject().getParent();
            String artifactId = p.getArtifactId();
            if ("maven.web-fragment.pom".equals(artifactId) || "archetype.web-base.extensions.pom".equals(artifactId)) {
                updateContextForWebFragment(monitor, serverConfiguration, iModule, "conf/Catalina/localhost/ROOT.xml", remove);
            } else if ("maven.users.module.pom".equals(artifactId) || "archetype.directory.extensions.pom".equals(artifactId)) {
                updateContextForWebFragment(monitor, serverConfiguration, iModule, "conf/Catalina/localhost/directory.xml", remove);
            }
        }
        
    }

    private void updateContextForWebFragment(IProgressMonitor monitor,
                                             IFolder serverConfiguration,
                                             IModule iModule,
                                             String contextFilePath,
                                             boolean remove)
        throws TransformerFactoryConfigurationError, CoreException {
        IFile contextFile = serverConfiguration.getFile(contextFilePath);
        if (contextFile.exists()) {
            try (InputStream contents = contextFile.getContents()) {

                Document doc = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder()
                        .parse(new InputSource(contents));
                Element contextEl = doc.getDocumentElement();
                final Element resources;
                NodeList resourcesNodeList = contextEl.getElementsByTagName("Resources");
                if (resourcesNodeList != null && resourcesNodeList.getLength() > 0)  {
                    resources = (Element) resourcesNodeList.item(0);
                } else {
                    resources = doc.createElement("Resources");
                    contextEl.appendChild(resources);
                }
                NodeList postResourcesNodeList = resources.getElementsByTagName("PostResources");
                if (postResourcesNodeList != null) {
                    boolean found = false;
                    String moduleBase = "${cobalt.webfragments}/" + iModule.getName();
                    for (int i = 0; i < postResourcesNodeList.getLength() && !found; i++) {
                        Element postResources = (Element) postResourcesNodeList.item(i);
                        Attr baseAttr = postResources.getAttributeNode("base");
                        found = baseAttr != null && baseAttr.getValue().equals(moduleBase);
                        if (found && remove) {
                            resources.removeChild(postResources);
                            break;
                        }
                    }
                    if (!found && !remove) {
                        Element postResources = doc.createElement("PostResources");
                        postResources.setAttribute("base", moduleBase);
                        postResources.setAttribute("className", "org.apache.catalina.webresources.DirResourceSet");
                        postResources.setAttribute("webAppMount", "/WEB-INF/lib");
                        resources.appendChild(postResources);
                    }
                }
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                Result outputTarget = new StreamResult(outputStream);
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                transformer.transform(new DOMSource(doc), outputTarget);
                InputStream is = new ByteArrayInputStream(outputStream.toByteArray());
                IFile backupFile = serverConfiguration.getFile(contextFilePath + ".bak");
                int i=0;
                while (backupFile.exists()) {
                    i++;
                    backupFile = serverConfiguration.getFile(contextFilePath + ".bak" + i);
                }
                IPath backupPath = backupFile.getFullPath();
                contextFile.move(backupPath, true, monitor);
                contextFile.create(is, true, monitor);
            } catch (Exception ex) {
                throw new CoreException(new Status(IStatus.ERROR, CobaltSDKPlugin.PLUGIN_ID,
                                                   "Unable to modify ROOT.xml file", ex));
            }
        }
    }

    private void removeWeb(IProgressMonitor monitor, IFolder serverConfiguration, IModule iModule)
        throws CoreException {
        final String fileName = iModule.getName() + ".xml.sample";
        IFile contextFile = serverConfiguration.getFile("conf/Catalina/localhost/" + fileName);
        if (contextFile.exists()) {
            contextFile.delete(true, monitor);
        }
    }

    private void addWeb(IProgressMonitor monitor, IFolder serverConfiguration, IModule iModule)
        throws CoreException, TransformerFactoryConfigurationError {
        IWebModule adapter = (IWebModule) iModule.loadAdapter(IWebModule.class, monitor);
        //String contextRoot = adapter.getContextRoot();
        final String fileName = iModule.getName() + ".xml.sample";
        /*if (contextRoot.equals("/")) {
            fileName = "ROOT.xml";
        } else {
            fileName = contextRoot + ".xml";
        }*/
   
        IFile contextFile = serverConfiguration.getFile("conf/Catalina/localhost/" + fileName);
        if (contextFile.exists()) {
            IFile backupFile = serverConfiguration.getFile("conf/Catalina/localhost/" + fileName + ".bak");
            int i=0;
            while (backupFile.exists()) {
                i++;
                backupFile = serverConfiguration.getFile("conf/Catalina/localhost/" + fileName + ".bak" + i);
            }
            IPath backupPath = backupFile.getFullPath();
            contextFile.move(backupPath, true, monitor);
        }
        if (!contextFile.exists()) {
            boolean found = false;
            for (IContainer c : adapter.getResourceFolders()) {
                IFolder f = (IFolder) c;
                IFile file = f.getFile("META-INF/context.xml");
                if (file.exists()) {
                    try (InputStream contents = file.getContents()) {
                        Document doc = DocumentBuilderFactory.newInstance()
                                .newDocumentBuilder()
                                .parse(new InputSource(contents));
                        Element contextEl = doc.getDocumentElement();
                        Comment helpComment = doc.createComment(HELP_COMMENT);
                        doc.insertBefore(helpComment, contextEl);
                        Attr docBaseAttr = contextEl.getAttributeNode("docBase");
                        if (docBaseAttr == null) {
                            docBaseAttr = doc.createAttribute("docBase");
                            contextEl.setAttributeNode(docBaseAttr);
                        }
                        docBaseAttr.setValue("${cobalt.webapps}/" + iModule.getName());
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        Result outputTarget = new StreamResult(outputStream);
                        Transformer transformer = TransformerFactory.newInstance().newTransformer();
                        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                        transformer.transform(new DOMSource(doc), outputTarget);
                        InputStream is = new ByteArrayInputStream(outputStream.toByteArray());
                        contextFile.create(is, true, monitor);
                    } catch (Exception ex) {
                        throw new CoreException(new Status(IStatus.ERROR, CobaltSDKPlugin.PLUGIN_ID, "Unable to modify context.xml files", ex));
                    }
                    found = true;
                    break;
                }
            }
            if (!found) {
                String contextString = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
                    + "\r\n<!--" + HELP_COMMENT + "-->"
                    + "\r\n<Context docBase=\"${cobalt.webapps}/" + iModule.getName() + "\"/>";
                InputStream stream = new ByteArrayInputStream(contextString.getBytes(StandardCharsets.UTF_8));
                contextFile.create(stream, true, monitor);
            }
        }
    }

}
