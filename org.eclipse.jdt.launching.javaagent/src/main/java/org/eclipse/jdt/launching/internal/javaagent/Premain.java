/*******************************************************************************
 * Copyright (c) 2011-2016 Igor Fedorenko
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Igor Fedorenko - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.launching.internal.javaagent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

import org.eclipse.jdt.launching.internal.weaving.ClassfileTransformer;

public class Premain {
	private static final ClassfileTransformer transformer = new ClassfileTransformer();

	public static void premain(@SuppressWarnings("unused") String agentArgs, Instrumentation inst) {
		// System.err.println("Advanced source lookup support loaded."); //$NON-NLS-1$

		inst.addTransformer(new ClassFileTransformer() {
			public byte[] transform(ClassLoader loader, final String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
				try {
					if (protectionDomain == null) {
						return null;
					}

					if (className == null) {
						return null;
					}

					final CodeSource codeSource = protectionDomain.getCodeSource();
					if (codeSource == null) {
						return null;
					}

					final URL locationUrl = codeSource.getLocation();
					if (locationUrl == null) {
						return null;
					}

					final String location = locationUrl.toExternalForm();

					return transformer.transform(classfileBuffer, location);
				}
				catch (Exception e) {
					System.err.print("Could not instrument class " + className + ": "); //$NON-NLS-1$ //$NON-NLS-2$
					e.printStackTrace(System.err);
				}
				return null;
			}
		});
	}
}
