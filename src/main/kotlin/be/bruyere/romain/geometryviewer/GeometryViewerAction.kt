import be.bruyere.romain.geometryviewer.GeometryViewerDialog
import be.bruyere.romain.geometryviewer.GeometryViewerException
import be.bruyere.romain.geometryviewer.CustomDebuggerCommandImpl
import com.intellij.debugger.DebuggerManagerEx
import com.intellij.debugger.engine.JavaValue
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.xdebugger.impl.ui.tree.actions.XDebuggerTreeActionBase
import com.jetbrains.jdi.IntegerValueImpl
import com.jetbrains.jdi.ObjectReferenceImpl
import com.jetbrains.jdi.StringReferenceImpl
import com.sun.jdi.Method
import com.sun.jdi.Value
import org.locationtech.jts.geom.*


private const val TO_TEXT_METHOD = "toText"
private const val GET_SRID_METHOD = "getSRID"

class GeometryViewerAction : AnAction(), DumbAware {

    // List of JTS classes that can be displayed in the viewer
    private val supportedClasses = listOf(
        Point::class,
        LineString::class,
        Polygon::class,
        MultiPoint::class,
        MultiLineString::class,
        MultiPolygon::class,
        GeometryCollection::class
    )

    override fun actionPerformed(event: AnActionEvent) {
        val supportedClassNames = supportedClasses.map { it.javaObjectType.name }
        val targetValue = getTargetValue(event)
        val targetObjectRef = toObjectReference(targetValue)
        if (targetValue != null &&
            targetObjectRef != null &&
            supportedClassNames.contains(targetObjectRef.referenceType().name())
        ) {
            val toTextMethod = getToTextMethod(targetObjectRef)
            val wkt = getWkt(event, targetObjectRef, targetValue, toTextMethod)
            val getSRIDMethod = getGetSRIDMethod(targetObjectRef)
            val srid = getSRID(event, targetObjectRef, targetValue, getSRIDMethod)
            val dialog = GeometryViewerDialog(wkt.value(), srid.value())
            dialog.show()
        } else if (targetObjectRef is StringReferenceImpl && isWKT(targetObjectRef.value())) {
            val dialog = GeometryViewerDialog(targetObjectRef.value())
            dialog.show()
        }
    }

    /**
     * Get the WKT representation of the geometry attached to the target object.
     */
    private fun getWkt(
        event: AnActionEvent,
        targetObjectRef: ObjectReferenceImpl,
        targetValue: JavaValue,
        toTextMethod: Method?
    ): StringReferenceImpl {
        val value = executeMethod(event, targetObjectRef, targetValue, toTextMethod)
        return value as? StringReferenceImpl ?: throw GeometryViewerException("No geometry is attached to the event")
    }

    /**
     * Get the SRID of the geometry attached to the target object.
     */
    private fun getSRID(
        event: AnActionEvent,
        targetObjectRef: ObjectReferenceImpl,
        targetValue: JavaValue,
        getSRIDMethod: Method?
    ): IntegerValueImpl {
        val value = executeMethod(event, targetObjectRef, targetValue, getSRIDMethod)
        return value as? IntegerValueImpl ?: throw GeometryViewerException("No geometry is attached to the event")
    }

    /**
     * Executes a method in the debug session context for the current project on the target object.
     */
    private fun executeMethod(
        event: AnActionEvent,
        targetObjectRef: ObjectReferenceImpl,
        targetValue: JavaValue,
        method: Method?
    ): Value? {
        val project = event.project ?: throw GeometryViewerException("No project attached to the event")
        val session = DebuggerManagerEx.getInstanceEx(project).context.debuggerSession
        val context = session?.process?.suspendManager?.pausedContext
        val managerThread = targetValue.evaluationContext.managerThread
        val command = CustomDebuggerCommandImpl(context, targetObjectRef, method)
        managerThread.invokeAndWait(command)
        return command.result
    }

    /**
     * Get the reference of the JTS Geometry.toText() method of the target object.
     * This method will be used to get the WKT representation of the geometry.
     */
    private fun getToTextMethod(targetObjectRef: ObjectReferenceImpl): Method? =
        targetObjectRef.referenceType().methodsByName(TO_TEXT_METHOD)[0]

    /**
     * Get the reference of the JTS Geometry.getSRID() method of the target object.
     * This method will be used to get the SRID of the geometry.
     */
    private fun getGetSRIDMethod(targetObjectRef: ObjectReferenceImpl): Method? =
        targetObjectRef.referenceType().methodsByName(GET_SRID_METHOD)[0]

    /**
     * Get the object reference of the selected node in the debugger tree.
     */
    private fun getTargetObjectReference(event: AnActionEvent): ObjectReferenceImpl? {
        val value = getTargetValue(event)
        return toObjectReference(value)
    }

    /**
     * Get the java value of the selected node in the debugger tree.
     */
    private fun getTargetValue(event: AnActionEvent): JavaValue? {
        val node = XDebuggerTreeActionBase.getSelectedNode(event.dataContext)
        val valueContainer = node?.valueContainer
        val value = valueContainer as? JavaValue
        return value
    }

    /**
     * Convert a JavaValue to an ObjectReferenceImpl
     */
    private fun toObjectReference(value: JavaValue?): ObjectReferenceImpl? {
        return value?.descriptor?.value as ObjectReferenceImpl?
    }

    /**
     * Enable the action only if the selected object is a JTS Geometry.
     */
    override fun update(event: AnActionEvent) {
        val targetObjectRef = getTargetObjectReference(event)

        if (targetObjectRef is StringReferenceImpl) {
            event.presentation.isEnabledAndVisible = isWKT(targetObjectRef.value())
        } else {
            getTargetObjectReference(event)?.referenceType()?.name()?.let {
                event.presentation.isEnabledAndVisible = supportedClasses.map { it.javaObjectType.name }.contains(it)
            }
        }
    }

    /**
     * Check if a string is a WKT
     */
    private fun isWKT(input: String): Boolean {
        val wktRegex = Regex(
            """^\s*(POINT|LINESTRING|POLYGON|MULTIPOINT|MULTILINESTRING|MULTIPOLYGON|GEOMETRYCOLLECTION)\s*\(\s*(.+)\s*\)\s*$"""
        )
        return wktRegex.matches(input)
    }
}