package scala.tools.nsc
package interactive

import collection.mutable.ArrayBuffer
import util.Position
import reporters.Reporter

case class Problem(pos: Position, msg: String, severityLevel: Int)

abstract class InteractiveReporter extends Reporter {

  def compiler: Global

  val otherProblems = new ArrayBuffer[Problem]

  override def info0(pos: Position, msg: String, severity: Severity, force: Boolean): Unit = try {
    severity.count += 1
    val problems =
      if (compiler eq null) {
        otherProblems
      } else if (pos.isDefined) {
        compiler.getUnit(pos.source) match {
          case Some(unit) =>
            compiler.debugLog(pos.source.file.name + ":" + pos.line + ": " + msg)
            unit.problems
          case None =>
            compiler.debugLog(pos.source.file.name + "[not loaded] :" + pos.line + ": " + msg)
            otherProblems
        }
      } else {
        compiler.debugLog("[no position] :" + msg)
        otherProblems
      }
    problems += Problem(pos, msg, severity.id)
  } catch {
    case ex: UnsupportedOperationException =>
  }

  override def reset() {
    super.reset()
    otherProblems.clear()
  }
}
