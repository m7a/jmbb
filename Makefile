# This is largely a private Makefile. Compiling is just this:
#
# 	javac -source 1.7 -target 1.7 $(find org ma -type f -name '*.java')
# 	jar cfve jmbb.jar ma.jmbb.Main org ma
#

LANG    = java
TARGET  = jmbb.jar
RES     = ma
XRES    = org README.txt Makefile
XFLAGS  = -source 1.7 -target 1.7
MAIN    = ma.jmbb.Main
SECTION = 32

include /usr/share/mdvl/make.mk
include /data/main/man/web/external/inc.mk

TESTDIR=/home/linux-fan/Ma_Sys.ma/Temp/file_actions/testbak
TESTSRC=$(TESTDIR)/src
TESTDST=$(TESTDIR)/dst
TESTBENCH=/usr/share/mdvl
PKGDIR=/data/main/mdvl/packages/raw/mdvl-java-applications/usr/share/mdvl/java

xz:
	javac $(XFLAGS) org/tukaani/xz/*.java
	javac $(XFLAGS) org/tukaani/xz/*/*.java

update-readme:
	cp /data/main/man/d5i/32/jmbb.d5i README.txt

test-create: jmbb
	-rm -r "$(TESTDST)"
	echo testwort | java -Xmx5G ma.jmbb.Main -o "$(TESTDST)" -i "$(TESTSRC)"

test-benchmark: jmbb
	-rm -r "$(TESTDST)"
	echo testwort | /usr/bin/time java -Xmx5G ma.jmbb.Main -o "$(TESTDST)" \
							-i "$(TESTBENCH)"

test-benchmark-7z:
	/usr/bin/time sh -c "tar -c \"$(TESTBENCH)\" | 7z a -t7z -m0=lzma2 \
				-mx=9 -mfb=64 -md=64m -ms=2g -l -ptestwort \
				-si -bd \"$(TESTDST)/archive.7z\""

test-restore: jmbb
	/usr/bin/time java -Xmx5G ma.jmbb.Main -r "$(TESTDIR)/restored" \
								-s "$(TESTDST)"

test-restore-7z:
	-rm -r "$(TESTDIR)/restored2"
	mkdir "$(TESTDIR)/restored2"
	/usr/bin/time sh -c "7z x -so \"$(TESTDST)/archive.7z\" -ptestwort | \
					tar -C \"$(TESTDIR)/restored2\" -x"

pkg: jar
	cp jmbb.jar "$(PKGDIR)/jmbb.jar"
	cp README.txt "$(PKGDIR)/jmbb_README.d5i"
