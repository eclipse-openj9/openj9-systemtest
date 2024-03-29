REM *******************************************************************************
REM Copyright (c) 2017, 2023 IBM Corp. and others
REM
REM This program and the accompanying materials are made available under the
REM terms of the Eclipse Public License 2.0 which accompanies this distribution
REM and is available at http://eclipse.org/legal/epl-2.0 or the Apache License,
REM Version 2.0 which accompanies this distribution and is available at
REM https://www.apache.org/licenses/LICENSE-2.0.
REM
REM This Source Code may also be made available under the following Secondary
REM Licenses when the conditions for such availability set forth in the
REM Eclipse Public License, v. 2.0 are satisfied: GNU General Public License,
REM version 2 with the GNU Classpath Exception [1] and GNU General Public License,
REM version 2 with the OpenJDK Assembly Exception [2].
REM
REM [1] https://www.gnu.org/software/classpath/license.html
REM [2] https://openjdk.org/legal/assembly-exception.html
REM
REM SPDX-License-Identifier: EPL-2.0 OR Apache-2.0 OR GPL-2.0-only WITH Classpath-exception-2.0 OR GPL-2.0-only WITH OpenJDK-assembly-exception-1.0
REM *******************************************************************************/

REM Save current directory
set currdir=%cd%
REM Clone stf
(IF NOT EXIST %USERPROFILE%\git mkdir %USERPROFILE%\git) && cd %USERPROFILE%\git && (IF EXIST stf rmdir /s /q stf) && git clone https://github.com/adoptium/stf.git stf
REM Clone aqa-systemtest
(IF NOT EXIST %USERPROFILE%\git mkdir %USERPROFILE%\git) && cd %USERPROFILE%\git && (IF EXIST aqa-systemtest rmdir /s /q aqa-systemtest) && git clone https://github.com/adoptium/aqa-systemtest.git aqa-systemtest
REM Clone openj9-systemtest
(IF NOT EXIST %USERPROFILE%\git mkdir %USERPROFILE%\git) && cd %USERPROFILE%\git && (IF EXIST openj9-systemtest rmdir /s /q openj9-systemtest) && git clone https://github.com/eclipse-openj9/openj9/openj9-systemtest.git openj9-systemtest
REM Configure (get prereqs)
cd %USERPROFILE%\git\openj9-systemtest\openj9.build
make configure
IF ERRORLEVEL 1 cd %currdir% && exit /B 1
REM Build
cd %USERPROFILE%\git\openj9-systemtest\openj9.build
make
IF ERRORLEVEL 1 cd %currdir% && exit /B 1
@echo openj9-systemtest repository build successful - to run the tests
@echo cd %USERPROFILE%\git\openj9-systemtest\openj9.build && make test
REM Restore current directory
cd %currdir%
exit /B 0
