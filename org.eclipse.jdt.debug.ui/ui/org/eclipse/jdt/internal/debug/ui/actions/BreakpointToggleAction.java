package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.IDebugViewAdapter;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.texteditor.IUpdate;

public abstract class BreakpointToggleAction extends Action implements IViewActionDelegate, IUpdate {
	
	private IStructuredSelection fCurrentSelection;
	private IAction fAction;
	
	public BreakpointToggleAction() {
		setEnabled(false);
	}

	/**
	 * @see Action#run()
	 */
	public void run() {
		run(null);
	}

	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart viewPart) {
		IDebugViewAdapter debugView = (IDebugViewAdapter)viewPart.getAdapter(IDebugViewAdapter.class);
		if (debugView != null) {
			// add myself to the debug view, such that my update method
			// will be called when a breakpoint changes
			debugView.setAction(getClass().getName(), this);
		}		
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		fAction= action;
		IStructuredSelection selection= getStructuredSelection();
		Iterator enum= selection.iterator();
		while (enum.hasNext()) {
			try {
				IJavaBreakpoint breakpoint= (IJavaBreakpoint) enum.next();
				doAction(breakpoint);
			} catch (CoreException e) {
				String title= ActionMessages.getString("BreakpointAction.Breakpoint_configuration_1"); //$NON-NLS-1$
				String message= ActionMessages.getString("BreakpointAction.Exceptions_occurred_attempting_to_modify_breakpoint._2"); //$NON-NLS-1$
				ErrorDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchWindow().getShell(), title, message, e.getStatus());
			}			
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection.isEmpty()) {
			fCurrentSelection= null;
		}
		else if (selection instanceof IStructuredSelection) {
			fCurrentSelection= (IStructuredSelection)selection;
			boolean enabled= fCurrentSelection.size() == 1 && isEnabledFor(fCurrentSelection.getFirstElement());
			action.setEnabled(enabled);
			if (enabled) {
				IBreakpoint breakpoint= (IBreakpoint)fCurrentSelection.getFirstElement();
				if (breakpoint instanceof IJavaBreakpoint) {
					try {
						action.setChecked(getToggleState((IJavaBreakpoint) breakpoint));
					} catch (CoreException e) {
					}
				}
			}
		}
	}

	/**
	 * Toggle the state of this action
	 */
	public abstract void doAction(IJavaBreakpoint breakpoint) throws CoreException;
	
	/**
	 * Returns whether this action is currently toggled on
	 */
	protected abstract boolean getToggleState(IJavaBreakpoint breakpoint) throws CoreException;
	
	/**
	 * Get the current selection
	 */
	protected IStructuredSelection getStructuredSelection() {
		return fCurrentSelection;
	}
	
	public abstract boolean isEnabledFor(Object element);
	
	/** 
	 * @see IUpdate#update()
	 */
	public void update() {
		if (fAction != null && fCurrentSelection != null) {
			selectionChanged(fAction, fCurrentSelection);
		}
	}	

	/**
	 * Get the breakpoint manager for the debug plugin
	 */
	protected IBreakpointManager getBreakpointManager() {
		return DebugPlugin.getDefault().getBreakpointManager();		
	}
	
	/**
	 * Get the breakpoint associated with the given marker
	 */
	protected IBreakpoint getBreakpoint(IMarker marker) {
		return getBreakpointManager().getBreakpoint(marker);
	}
}

