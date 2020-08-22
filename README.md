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