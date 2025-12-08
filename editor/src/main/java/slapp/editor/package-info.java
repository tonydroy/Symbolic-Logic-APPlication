/**
 * Editor for creating and editing exercises in logic.
 *
 * The editor package is itself broken into multiple smaller packages.<br><br>
 *
 * 1. The slapp.editor makes heavy use of (a slightly modified version of) the <a href = "https://github.com/gluonhq/rich-text-area">Rich Text Area</a> (RTA).
 * {@link slapp.editor.decorated_rta} generates instances of the RTA with controls including toolbars.<br><br>
 *
 * 2. {@link slapp.editor.main_window} sets the view and control of the editor as a whole, loading exercises and assignments.<br><br>
 *
 * 3.  After that, there are separate packages for the different exercise types.  Each includes <em>create</em>, <em>exercise</em>,
 * <em>model</em>, and <em>view</em> files along with any supporting elements.  The 'exercise' file is the exercise's controller.
 */
package slapp.editor;