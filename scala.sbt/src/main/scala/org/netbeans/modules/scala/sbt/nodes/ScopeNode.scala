package org.netbeans.modules.scala.sbt.nodes

import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import javax.swing.SwingUtilities
import org.netbeans.api.project.Project
import org.netbeans.modules.scala.sbt.classpath.SBTController
import org.openide.filesystems.FileUtil
import org.openide.nodes.Children
import org.openide.nodes.Node
import org.openide.util.NbBundle

class ScopeNode(project: Project, scope: String) extends AbstractFolderNode(new ScopesChildren(project, scope)) {

  override
  def getDisplayName = NbBundle.getMessage(classOf[ScopeNode], "CTL_Scope_" + scope)

  override
  def getName = scope

  override
  protected def getBadge = Icons.ICON_LIBARARIES_BADGE
}

private class ScopesChildren(project: Project, scope: String) extends Children.Keys[ArtifactInfo] with PropertyChangeListener {

  setKeys

  override
  protected def createNodes(key: ArtifactInfo): Array[Node] = {
    Array(new ArtifactNode(key, project))
  }

  def propertyChange(evt: PropertyChangeEvent) {
    evt.getPropertyName match {
      case SBTController.SBT_LIBRARY_RESOLVED => setKeys
      case _ =>
    }
  }

  private def setKeys {
    val sbtController = project.getLookup.lookup(classOf[SBTController])
    val artifacts = sbtController.getResolvedLibraries(scope) map FileUtil.toFileObject filter {fo => 
      fo != null && FileUtil.isArchiveFile(fo)
    } map {fo =>
      ArtifactInfo(fo.getNameExt, "", "", FileUtil.toFile(fo), null, null)
    }
    
    SwingUtilities.invokeLater(new Runnable() {
        def run() {
          setKeys(artifacts.sortBy(_.name))
        }
      })
  }
}