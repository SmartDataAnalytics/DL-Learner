To create a debian package:
	1. use dch (apt-get install devscripts) to modify the changelog file in src/debian
	2. just copy the comments (only the comments) from the changelog file to CHANGES.txt
		In CHANGES.txt the first line must be like release date=20:13 13.10.2009,version=0.2,urgency=low,by=Jens Lehmann,distribution=staging
		paste the comments after this with one whitespace at begin of the line
	3. compile with mvn clean install -Pdebpackage
	4. the deb-package will be generate in target with a .changes file too
	5. sign the package and upload it to the stack