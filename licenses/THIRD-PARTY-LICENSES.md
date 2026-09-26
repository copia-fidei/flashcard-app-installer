# Third-Party Licenses

## Apache Commons Codec
Copyright 2002-2026 The Apache Software Foundation
License: Apache License 2.0
Source code: https://github.com/apache/commons-codec

## Apache Commons Compress
Copyright 2002-2026 The Apache Software Foundation
License: Apache License 2.0
Source code: https://github.com/apache/commons-compress

## Apache Commons IO
Copyright 2002-2026 The Apache Software Foundation
License: Apache License 2.0
Source code: https://github.com/apache/commons-io

## Apache Commons Lang
Copyright 2001-2026 The Apache Software Foundation
License: Apache License 2.0
Source code: https://github.com/apache/commons-lang

## FlatLaf
Including FlatLaf Extras.

License: Apache License 2.0
Source code: https://github.com/JFormDesigner/FlatLaf

## JetBrains Java Annotations
License: Apache License 2.0
Source code: https://github.com/JetBrains/java-annotations

## Kotlin Stdlib
License: Apache License 2.0
Copyright 2010-2024 JetBrains s.r.o and respective authors and developers
Source code: https://github.com/jetbrains/kotlin

## Java Native Access
Including Java Native Access Platform.

Version 5.14.0
Apache License, Version 2.0
Source code: https://github.com/java-native-access

## Checker Qual
Source code: https://github.com/typetools/checker-framework/tree/master/checker-qual

Checker Framework qualifiers
Copyright 2004-present by the Checker Framework developers

MIT License:

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.


## JSVG
Source code: https://github.com/weisJ/jsvg

MIT License

Copyright (c) 2021-2024 Jannis Weis

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

## SLF4J API Module
Source code: https://github.com/qos-ch/slf4j/blob/master/slf4j-api

Copyright (c) 2004-2023 QOS.ch
All rights reserved.

Permission is hereby granted, free  of charge, to any person obtaining
a  copy  of this  software  and  associated  documentation files  (the
"Software"), to  deal in  the Software without  restriction, including
without limitation  the rights to  use, copy, modify,  merge, publish,
distribute,  sublicense, and/or sell  copies of  the Software,  and to
permit persons to whom the Software  is furnished to do so, subject to
the following conditions:

The  above  copyright  notice  and  this permission  notice  shall  be
included in all copies or substantial portions of the Software.

THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

## PostgreSQL JDBC Driver
License: BSD 2-Clause "Simplified" License
Source code: https://github.com/pgjdbc/pgjdbc

Copyright (c) 1997, PostgreSQL Global Development Group
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.

## pty4j
License: Eclipse Public License 1.0
Source code: https://github.com/JetBrains/pty4j

# Third-Party Binary Modifications
JPMS module descriptors have been added to the following JARs using the ModiTect Maven Plugin.
- JNA (net.java.dev.jna:jna)
- JNA Platform (net.java.dev.jna:jna-platform)
- PostgreSQL JDBC Driver (org.postgresql:postgresql)
- pty4j (org.jetbrains.pty4j:pty4j) 

The resulting modules are incorporated into the runtime image by `jlink`.

# JPackage
The Debian package in /releases is generated using jpackage and includes the third-party dependencies listed above.
Some of these dependencies have been modified before running jpackage; see "Third-Party Binary Modifications".

## OpenJDK
The Debian package includes Ubuntu's OpenJDK 25 runtime (25.0.4.1+1-1-22.04.4-Ubuntu).
The OpenJDK runtime's applicable copyright notices and license texts can be viewed under {APPNAME}/lib/runtime/legal/ 
after installation of the Debian package.

## Third-Party Binary Modifications
JPMS module descriptors have been added to the following third-party JARs using the ModiTect Maven Plugin
before being included in the Debian package:
- JNA (net.java.dev.jna:jna)
- JNA Platform (net.java.dev.jna:jna-platform)
- PostgreSQL JDBC Driver (org.postgresql:postgresql)
- pty4j (org.jetbrains.pty4j:pty4j)


# Icons
## hourglass-done-svgrepo-com.svg
License: Apache License 2.0
Download link: https://www.svgrepo.com/svg/396666/hourglass-done
Icons pack: Noto Emoji
Source: https://github.com/googlefonts/noto-emoji/blob/main/2D/svg/emoji_u231b.svg
Source license: https://github.com/googlefonts/noto-emoji/blob/main/2D/svg/LICENSE

Copyright 2013 Google, Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

## Siemens Industrial Experience Icons
- Icon: warning-filled.svg  
Download link: https://www.svgrepo.com/svg/486508/warning-filled
- Icon: error-svgrepo-com.svg  
Download link: https://www.svgrepo.com/svg/486662/error
- Icon: info-svgrepo-com.svg  
Download link: https://www.svgrepo.com/svg/486702/info

Source: https://github.com/siemens/ix-icons

MIT License

Copyright (c) 2022 Siemens AG

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

## Public Domain Icons
The remaining icons are in the public domain.

# Apache Karaf
apache-karaf-4.4.11.tar.gz is included in the installer.
License: Apache License 2.0
Download: https://karaf.apache.org/download
