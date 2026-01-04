SnipSnap 
Copyright (c) 2000-2006 Fraunhofer Gesellschaft
Fraunhofer Institute for Computer Architecture and Software Technology
All Rights Reserved. See License Agreement for terms and conditions of use.
Responsible Authors: Stephan J. Schmidt, Matthias L. Jugel.

SnipSnap is a personal content management system in a box.
It is available under the terms and conditions of the GNU
General Public License (see license.txt).

The latest version is always available here:

	http://snipsnap.org/

Installation instructions can be found here:

	http://snipsnap.org/space/install

Enjoy your SnipSnap!

-----------------------------------------------------------------------

### Build & Run ('as is')

- docker build -t snipsnap .
- docker run -p 8668:8668 snipsnap

The server ideally started and shows a weired adress like
http://9e897a7bdf64:8668/install/76f8b
change to
http://localhost:8668/install/76f8b

![Screenshot](/screenshot.png?raw=true "Screenshot")

-----------------------------------------------------------------------

## Libraries & Technologies

SnipSnap relies on a robust set of open-source libraries. Here is a detailed breakdown of their usage within the codebase:

### Core Server & Web Container
*   **Jetty (`org.mortbay.jetty`):** Used as the embedded HTTP server and Servlet container. Key components like `AppServer` and `ApplicationLoader` directly interface with Jetty to lifecycle manage the web application.
*   **XML-RPC (`org.apache.xmlrpc`):** Heavily used for remote administration and inter-service communication (e.g., `AdminXmlRpcHandler`, `SnipSnapHandler`). It powers the remote API for administering the wiki.

### Search & Content Rendering
*   **Radeox (`org.radeox`):** The heart of the wiki rendering engine. It is used extensively throughout the application (over 270 references) to parse and render SnipSnap's wiki syntax, handle macros, and filter content.
*   **Lucene (`org.apache.lucene`):** Provides the underlying full-text search capabilities for wiki pages (Snips) and labels.
*   **J2H:** Used for syntax highlighting of code blocks within the wiki (Java2HTML).

### Data & Storage
*   **JDBC & SQL (`java.sql`):** Standard JDBC interfaces are used for database interactions, supporting different backends like MySQL, PostgreSQL, and the embedded MckoiDB.
*   **Dom4j (`org.dom4j`):** Used extensively for XML processing, particularly for importing/exporting data (`XMLSnipExport`, `XMLSnipImport`) and handling serialization.

### Frameworks & Dependency Injection
*   **PicoContainer (`org.picocontainer`):** Used for lightweight dependency injection, managing component lifecycles and wiring together core services.
*   **Spring Framework (`org.springframework`):** Present in the codebase for dependency injection and application context management, co-existing with PicoContainer.

### Networking & Integration
*   **Smack (`org.jivesoftware.smack`):** Implements the XMPP (Jabber) bot functionality (`JabberBot`), allowing the wiki to interact with IM networks.
*   **JavaMail (`javax.mail`):** Handles email notifications and fetching content via email (`PostDaemon`, `ReadMail`).
*   **Commons HttpClient:** Included for making outbound HTTP requests, likely for features like pinging other services or fetching remote feeds.

### Utilities
*   **Rome:** Handles RSS and Atom syndication feeds.
*   **Jena:** Semantic Web framework used for RDF/DAML serialization.
*   **Groovy:** Integrated for scripting support, allowing dynamic macro loading and execution.