/*******************************************************************************
* Copyright (c) 2017, 2023 IBM Corp.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which accompanies this distribution
* and is available at http://eclipse.org/legal/epl-2.0 or the Apache License, 
* Version 2.0 which accompanies this distribution and is available at 
* https://www.apache.org/licenses/LICENSE-2.0.
* 
* This Source Code may also be made available under the following Secondary
* Licenses when the conditions for such availability set forth in the 
* Eclipse Public License, v. 2.0 are satisfied: GNU General Public License,
* version 2 with the GNU Classpath Exception [1] and GNU General Public License,
* version 2 with the OpenJDK Assembly Exception [2].
* 
* [1] https://www.gnu.org/software/classpath/license.html
* [2] https://openjdk.org/legal/assembly-exception.html
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0 OR GPL-2.0-only WITH Classpath-exception-2.0 OR GPL-2.0-only WITH OpenJDK-assembly-exception-1.0
*******************************************************************************/

package net.openj9.stf.sharedClasses;

import net.adoptopenjdk.stf.extensions.core.StfCoreExtension;
import net.adoptopenjdk.stf.plugin.interfaces.StfPluginRootInterface;
import net.adoptopenjdk.stf.runner.modes.HelpTextGenerator;


/**
 * This interface defines a SVT Shared Classes interface for the IBM Shared Classes test plugins.
 * 
 * It provides access to StfCoreExtension and adds access to StftSharedClassesExtension,
 * which provides extra STF capabilities for testing the IBM Shared Classes implementation.
 */
public interface SharedClassesPluginInterface extends StfPluginRootInterface {
	public void help(HelpTextGenerator help) throws Exception;

	public void pluginInit(StfCoreExtension stfCore, StfSharedClassesExtension sharedClasses) throws Exception;

	public void setUp(StfCoreExtension stfCore, StfSharedClassesExtension sharedClasses) throws Exception;
	
	public void tearDown(StfCoreExtension stfCore, StfSharedClassesExtension sharedClasses) throws Exception;
}
